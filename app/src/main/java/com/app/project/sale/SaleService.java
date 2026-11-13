package com.app.project.sale;

import com.app.project.client.ClientRepository;
import com.app.project.products.Product;
import com.app.project.products.ProductRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Regras de negócio das vendas: montar a venda a partir do catálogo, calcular o total,
// gerar as parcelas e controlar os pagamentos.
public class SaleService {

    // Cada parcela vence 30 dias depois da anterior, contando a partir da data da compra.
    private static final int DAYS_BETWEEN_INSTALLMENTS = 30;

    // Limite de segurança: evita que um valor absurdo gere milhares de linhas no banco.
    private static final int MAX_INSTALLMENTS = 120;

    // Valores monetários com 2 casas decimais.
    private static final int MONEY_SCALE = 2;

    private final SaleRepository saleRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    public SaleService(SaleRepository saleRepository,
                       ClientRepository clientRepository,
                       ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
    }

    // =========================================================================================
    // Consultas de vendas
    // =========================================================================================

    public SaleWithDetails getSaleWithDetailsById(long id) {
        return saleRepository.findWithDetailsById(id);
    }

    public SaleWithProducts getSaleWithProductsById(long id) {
        return saleRepository.findWithProductsById(id);
    }

    // Retorna null se a venda não existir (o controller responde 404).
    // Uma venda existente sempre tem itens, então lista vazia não é usada para indicar "não encontrada".
    public List<SaleItemWithProduct> getSaleItems(long saleId) {

        if (!saleRepository.existsById(saleId)) {
            return null;
        }
        return saleRepository.findItemsBySale(saleId);
    }

    public List<Installment> getSaleInstallments(long saleId) {

        if (!saleRepository.existsById(saleId)) {
            return null;
        }
        return saleRepository.findInstallmentsBySale(saleId);
    }

    public List<SaleWithDetails> getAllSales() {
        return saleRepository.findAll();
    }

    public List<SaleWithDetails> getSalesFiltered(Long clientId, LocalDate startDate, LocalDate endDate) {
        return saleRepository.findFiltered(clientId, startDate, endDate);
    }

    public List<SaleWithDetails> getSalesPaginated(Long clientId, LocalDate startDate, LocalDate endDate,
                                                   int page, int size) {
        return saleRepository.findFilteredPage(clientId, startDate, endDate, page, size);
    }

    // =========================================================================================
    // Consultas de parcelas
    // =========================================================================================

    public Installment getInstallmentById(long id) {
        return saleRepository.findInstallmentById(id);
    }

    public List<Installment> getOverdueInstallments() {
        return saleRepository.findOverdueInstallments(LocalDate.now());
    }

    public List<Installment> getPendingInstallmentsBetween(LocalDate startDate, LocalDate endDate) {
        return saleRepository.findPendingInstallmentsBetween(startDate, endDate);
    }

    // =========================================================================================
    // Criação da venda
    // =========================================================================================

    // O controller já validou o formato (campos obrigatórios, quantidades > 0).
    // Aqui ficam as regras que dependem do banco e o cálculo de valores e parcelas.
    public SaleWithDetails createSale(SaleRequestDto request) {

        if (clientRepository.findById(request.getClientId()) == null) {
            throw new IllegalArgumentException("Cliente não encontrado.");
        }

        LocalDate purchaseDate = request.getPurchaseDate() != null ? request.getPurchaseDate() : LocalDate.now();

        if (purchaseDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data da compra não pode ser futura.");
        }

        int installmentCount = request.getInstallmentCount() != null ? request.getInstallmentCount() : 1;

        if (installmentCount > MAX_INSTALLMENTS) {
            throw new IllegalArgumentException("O número máximo de parcelas é " + MAX_INSTALLMENTS + ".");
        }

        List<SaleItem> items = buildItems(request.getItems());
        BigDecimal total = calculateTotal(items);

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O total da venda deve ser maior que zero.");
        }

        Sale sale = new Sale();
        sale.clientId = request.getClientId();
        sale.purchaseDate = purchaseDate;
        sale.createdAt = Instant.now();
        sale.totalPrice = total;

        List<Installment> installments = buildInstallments(total, installmentCount, purchaseDate);

        return saleRepository.createSale(sale, items, installments);
    }

    // Monta os itens a partir do catálogo: o nome e o preço vêm do produto cadastrado,
    // nunca do frontend. Se o mesmo produto vier repetido, as quantidades são somadas.
    private List<SaleItem> buildItems(List<SaleItemRequestDto> requestedItems) {

        // LinkedHashMap mantém a ordem em que os produtos foram informados.
        Map<Long, Integer> quantityByProduct = new LinkedHashMap<>();

        for (SaleItemRequestDto requested : requestedItems) {
            quantityByProduct.merge(requested.getProductId(), requested.getQuantity(), Integer::sum);
        }

        List<SaleItem> items = new ArrayList<>(quantityByProduct.size());

        for (Map.Entry<Long, Integer> entry : quantityByProduct.entrySet()) {

            Product product = productRepository.findById(entry.getKey());

            if (product == null) {
                throw new IllegalArgumentException("Produto " + entry.getKey() + " não encontrado.");
            }

            if (product.getPrice() == null) {
                throw new IllegalArgumentException("O produto \"" + product.getName() + "\" está sem preço cadastrado.");
            }

            SaleItem item = new SaleItem();
            item.productId = product.getProductId();
            item.productName = product.getName();
            item.quantity = entry.getValue();
            item.unitPrice = product.getPrice();

            items.add(item);
        }

        return items;
    }

    private BigDecimal calculateTotal(List<SaleItem> items) {

        BigDecimal total = BigDecimal.ZERO;

        for (SaleItem item : items) {
            total = total.add(item.unitPrice.multiply(BigDecimal.valueOf(item.quantity)));
        }

        return total.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    // Divide o total em parcelas iguais, arredondando para baixo, e coloca a diferença
    // de centavos na primeira parcela. Ex.: R$ 100,00 em 3x → 33,34 + 33,33 + 33,33.
    // A parcela N vence (30 × N) dias após a compra.
    private List<Installment> buildInstallments(BigDecimal total, int count, LocalDate purchaseDate) {

        BigDecimal baseAmount = total.divide(BigDecimal.valueOf(count), MONEY_SCALE, RoundingMode.DOWN);
        BigDecimal remainder = total.subtract(baseAmount.multiply(BigDecimal.valueOf(count)));

        List<Installment> installments = new ArrayList<>(count);

        for (int number = 1; number <= count; number++) {

            Installment installment = new Installment();
            installment.number = number;
            installment.dueDate = purchaseDate.plusDays((long) DAYS_BETWEEN_INSTALLMENTS * number);
            installment.amount = (number == 1) ? baseAmount.add(remainder) : baseAmount;

            installments.add(installment);
        }

        return installments;
    }

    // =========================================================================================
    // Pagamentos
    // =========================================================================================

    // Retorna null se a parcela não existir (o controller responde 404).
    // Lança IllegalStateException se já estiver paga (o controller responde 409).
    public Installment registerPayment(long installmentId, PaymentRequestDto request) {

        Installment installment = saleRepository.findInstallmentById(installmentId);

        if (installment == null) {
            return null;
        }

        LocalDate paidAt = request.getPaidAt() != null ? request.getPaidAt() : LocalDate.now();

        if (paidAt.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data de pagamento não pode ser futura.");
        }

        BigDecimal paidAmount = request.getPaidAmount() != null
                ? request.getPaidAmount().setScale(MONEY_SCALE, RoundingMode.HALF_UP)
                : installment.amount;

        // O UPDATE só altera a linha se a parcela ainda não estiver paga (operação atômica).
        if (!saleRepository.registerPayment(installmentId, paidAt, paidAmount)) {
            throw new IllegalStateException("Esta parcela já está paga.");
        }

        return saleRepository.findInstallmentById(installmentId);
    }

    // Retorna null se a parcela não existir.
    // Lança IllegalStateException se ela não estiver paga.
    public Installment cancelPayment(long installmentId) {

        if (saleRepository.findInstallmentById(installmentId) == null) {
            return null;
        }

        if (!saleRepository.cancelPayment(installmentId)) {
            throw new IllegalStateException("Esta parcela não está paga.");
        }

        return saleRepository.findInstallmentById(installmentId);
    }

    // =========================================================================================
    // Remoção
    // =========================================================================================

    // Itens e parcelas são removidos junto pelo CASCADE do banco.
    public boolean deleteSale(long id) {
        return saleRepository.deleteSale(id);
    }
}
