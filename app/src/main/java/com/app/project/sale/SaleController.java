package com.app.project.sale;

import com.app.project.shared.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;


public class SaleController {

    private final SaleService saleService;
    private final Gson gson;

    private static final String BASE_URI = "/api/v1/sales";
    private static final String INSTALLMENTS_URI = BASE_URI + "/installments";
    private static final String OVERDUE_URI = INSTALLMENTS_URI + "/overdue";
    private static final String PENDING_URI = INSTALLMENTS_URI + "/pending";

    private static final Pattern SALE_BY_ID_PATTERN =
            Pattern.compile("^" + BASE_URI + "/(\\d+)$");
    private static final Pattern SALE_ITEMS_PATTERN =
            Pattern.compile("^" + BASE_URI + "/(\\d+)/items$");
    private static final Pattern SALE_INSTALLMENTS_PATTERN =
            Pattern.compile("^" + BASE_URI + "/(\\d+)/installments$");
    private static final Pattern INSTALLMENT_BY_ID_PATTERN =
            Pattern.compile("^" + INSTALLMENTS_URI + "/(\\d+)$");
    private static final Pattern INSTALLMENT_PAYMENT_PATTERN =
            Pattern.compile("^" + INSTALLMENTS_URI + "/(\\d+)/payment$");

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
        this.gson = GsonFactory.create();
    }

    

    public Response handleGetRequest(IHTTPSession session) {

        String uri = session.getUri();
        Map<String, List<String>> params = session.getParameters();

        try {
            Matcher matcher;

            if ((matcher = SALE_BY_ID_PATTERN.matcher(uri)).matches()) {
                return fetchSaleById(Long.parseLong(matcher.group(1)), params);
            }

            if ((matcher = SALE_ITEMS_PATTERN.matcher(uri)).matches()) {
                return fetchSaleItems(Long.parseLong(matcher.group(1)));
            }

            if ((matcher = SALE_INSTALLMENTS_PATTERN.matcher(uri)).matches()) {
                return fetchSaleInstallments(Long.parseLong(matcher.group(1)));
            }

            if (OVERDUE_URI.equals(uri)) {
                return jsonResponse(Status.OK, saleService.getOverdueInstallments());
            }

            if (PENDING_URI.equals(uri)) {
                return fetchPendingInstallments(params);
            }

            if ((matcher = INSTALLMENT_BY_ID_PATTERN.matcher(uri)).matches()) {
                return fetchInstallmentById(Long.parseLong(matcher.group(1)));
            }

            if (BASE_URI.equals(uri)) {
                return fetchSales(params);
            }

            return errorResponse(Status.NOT_FOUND, "Endpoint GET não encontrado.");

        } catch (NumberFormatException e) {
            return errorResponse(Status.BAD_REQUEST, "Parâmetro numérico inválido.");
        } catch (DateTimeParseException e) {
            return errorResponse(Status.BAD_REQUEST, "Data inválida. Use o formato AAAA-MM-DD.");
        } catch (IllegalArgumentException e) {
            return errorResponse(Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(Status.INTERNAL_ERROR, "Erro interno ao processar requisição.");
        }
    }

    private Response fetchSales(Map<String, List<String>> params) {

        Long clientId = optionalLongParam(params, "clientId");
        LocalDate startDate = optionalDateParam(params, "startDate");
        LocalDate endDate = optionalDateParam(params, "endDate");

        validatePeriod(startDate, endDate, false);

        if (params.containsKey("page") || params.containsKey("size")) {

            int page = Integer.parseInt(params.getOrDefault("page", List.of("1")).get(0));
            int size = Integer.parseInt(params.getOrDefault("size", List.of("10")).get(0));

            if (page < 1 || size < 1) {
                return errorResponse(Status.BAD_REQUEST, "Os parâmetros page e size devem ser maiores que zero.");
            }

            return jsonResponse(Status.OK,
                    saleService.getSalesPaginated(clientId, startDate, endDate, page, size));
        }

        if (clientId != null || startDate != null) {
            return jsonResponse(Status.OK, saleService.getSalesFiltered(clientId, startDate, endDate));
        }

        return jsonResponse(Status.OK, saleService.getAllSales());
    }

    // Mesmo padrão do ClientController (?withSales=true): o parâmetro escolhe qual POJO retornar.
    private Response fetchSaleById(long saleId, Map<String, List<String>> params) {

        if (booleanParam(params, "withProducts")) {
            var saleWithProducts = saleService.getSaleWithProductsById(saleId);

            if (saleWithProducts == null) {
                return errorResponse(Status.NOT_FOUND, "Venda não encontrada.");
            }
            return jsonResponse(Status.OK, saleWithProducts);
        }

        var saleWithDetails = saleService.getSaleWithDetailsById(saleId);

        if (saleWithDetails == null) {
            return errorResponse(Status.NOT_FOUND, "Venda não encontrada.");
        }
        return jsonResponse(Status.OK, saleWithDetails);
    }

    private Response fetchSaleItems(long saleId) {

        var items = saleService.getSaleItems(saleId);

        if (items == null) {
            return errorResponse(Status.NOT_FOUND, "Venda não encontrada.");
        }
        return jsonResponse(Status.OK, items);
    }

    private Response fetchSaleInstallments(long saleId) {

        var installments = saleService.getSaleInstallments(saleId);

        if (installments == null) {
            return errorResponse(Status.NOT_FOUND, "Venda não encontrada.");
        }
        return jsonResponse(Status.OK, installments);
    }

    private Response fetchInstallmentById(long installmentId) {

        var installment = saleService.getInstallmentById(installmentId);

        if (installment == null) {
            return errorResponse(Status.NOT_FOUND, "Parcela não encontrada.");
        }
        return jsonResponse(Status.OK, installment);
    }

    private Response fetchPendingInstallments(Map<String, List<String>> params) {

        LocalDate startDate = optionalDateParam(params, "startDate");
        LocalDate endDate = optionalDateParam(params, "endDate");

        validatePeriod(startDate, endDate, true);

        return jsonResponse(Status.OK, saleService.getPendingInstallmentsBetween(startDate, endDate));
    }

   
    public Response handlePostRequest(IHTTPSession session) {

        String uri = session.getUri();

        try {
            Matcher matcher;

            if (BASE_URI.equals(uri)) {
                return createSale(session);
            }

            if ((matcher = INSTALLMENT_PAYMENT_PATTERN.matcher(uri)).matches()) {
                return registerPayment(session, Long.parseLong(matcher.group(1)));
            }

            // Venda já registrada não é editada: alterar itens mudaria o total e as parcelas,
            
            if (SALE_BY_ID_PATTERN.matcher(uri).matches()) {
                return errorResponse(Status.METHOD_NOT_ALLOWED,
                        "Vendas não podem ser alteradas. Exclua a venda e registre-a novamente.");
            }

            return errorResponse(Status.NOT_FOUND, "Endpoint não encontrado.");

        } catch (NumberFormatException e) {
            return errorResponse(Status.BAD_REQUEST, "Parâmetro numérico inválido.");
        } catch (JsonParseException e) {
            
            boolean dateError = e.getCause() instanceof DateTimeParseException;
            return errorResponse(Status.BAD_REQUEST, dateError ? e.getMessage() : "JSON mal formatado.");
        } catch (IllegalStateException e) {
            // Regra de negócio violada pelo estado atual (ex: parcela já paga).
            return errorResponse(Status.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return errorResponse(Status.BAD_REQUEST, e.getMessage());
        } catch (IOException | NanoHTTPD.ResponseException e) {
            return errorResponse(Status.INTERNAL_ERROR, "Erro de I/O ao processar a requisição.");
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(Status.INTERNAL_ERROR, "Erro interno ao processar a requisição.");
        }
    }

    private Response createSale(IHTTPSession session) throws IOException, NanoHTTPD.ResponseException {

        SaleRequestDto saleRequest = parseRequestBody(session, SaleRequestDto.class, true);

        if (saleRequest.getClientId() == null || saleRequest.getClientId() < 1) {
            return errorResponse(Status.BAD_REQUEST, "O clientId é obrigatório.");
        }

        if (saleRequest.getItems() == null || saleRequest.getItems().isEmpty()) {
            return errorResponse(Status.BAD_REQUEST, "Informe ao menos um item na venda.");
        }

        for (SaleItemRequestDto item : saleRequest.getItems()) {

            if (item == null || item.getProductId() == null || item.getProductId() < 1) {
                return errorResponse(Status.BAD_REQUEST, "Cada item precisa de um productId válido.");
            }

            if (item.getQuantity() == null || item.getQuantity() < 1) {
                return errorResponse(Status.BAD_REQUEST, "A quantidade de cada item deve ser maior que zero.");
            }
        }

        if (saleRequest.getInstallmentCount() != null && saleRequest.getInstallmentCount() < 1) {
            return errorResponse(Status.BAD_REQUEST, "O número de parcelas deve ser maior que zero.");
        }

        var createdSale = saleService.createSale(saleRequest);

        if (createdSale == null) {
            return errorResponse(Status.INTERNAL_ERROR, "Não foi possível registrar a venda.");
        }

        return jsonResponse(Status.CREATED, createdSale);
    }

    private Response registerPayment(IHTTPSession session, long installmentId)
            throws IOException, NanoHTTPD.ResponseException {

        // Corpo é opcional. Sem corpo, a parcela é quitada hoje pelo valor integral.
        PaymentRequestDto paymentRequest = parseRequestBody(session, PaymentRequestDto.class, false);

        if (paymentRequest.getPaidAmount() != null
                && paymentRequest.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return errorResponse(Status.BAD_REQUEST, "O valor pago deve ser maior que zero.");
        }

        var paidInstallment = saleService.registerPayment(installmentId, paymentRequest);

        if (paidInstallment == null) {
            return errorResponse(Status.NOT_FOUND, "Parcela não encontrada.");
        }

        return jsonResponse(Status.OK, paidInstallment);
    }

    
    public Response handleDeleteRequest(IHTTPSession session) {

        String uri = session.getUri();

        try {
            Matcher matcher;

            if ((matcher = SALE_BY_ID_PATTERN.matcher(uri)).matches()) {

                boolean removed = saleService.deleteSale(Long.parseLong(matcher.group(1)));

                if (!removed) {
                    return errorResponse(Status.NOT_FOUND, "Venda não encontrada.");
                }
                return NanoHTTPD.newFixedLengthResponse(Status.NO_CONTENT, "application/json; charset=UTF-8", "");
            }

            if ((matcher = INSTALLMENT_PAYMENT_PATTERN.matcher(uri)).matches()) {

                var installment = saleService.cancelPayment(Long.parseLong(matcher.group(1)));

                if (installment == null) {
                    return errorResponse(Status.NOT_FOUND, "Parcela não encontrada.");
                }
                return jsonResponse(Status.OK, installment);
            }

            return errorResponse(Status.NOT_FOUND, "Endpoint não encontrado.");

        } catch (NumberFormatException e) {
            return errorResponse(Status.BAD_REQUEST, "ID inválido.");
        } catch (IllegalStateException e) {
            
            return errorResponse(Status.CONFLICT, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(Status.INTERNAL_ERROR, "Erro interno ao processar a requisição.");
        }
    }


    private String firstParam(Map<String, List<String>> params, String name) {

        List<String> values = params.get(name);

        if (values == null || values.isEmpty() || values.get(0).trim().isEmpty()) {
            return null;
        }
        return values.get(0).trim();
    }

    private Long optionalLongParam(Map<String, List<String>> params, String name) {
        String value = firstParam(params, name);
        return value == null ? null : Long.parseLong(value);
    }

    private LocalDate optionalDateParam(Map<String, List<String>> params, String name) {
        String value = firstParam(params, name);
        return value == null ? null : LocalDate.parse(value);
    }

    private boolean booleanParam(Map<String, List<String>> params, String name) {
        return "true".equalsIgnoreCase(firstParam(params, name));
    }

    // startDate e endDate andam juntos e precisam estar em ordem.
    private void validatePeriod(LocalDate startDate, LocalDate endDate, boolean required) {

        if (startDate == null && endDate == null) {
            if (required) {
                throw new IllegalArgumentException("Informe os parâmetros startDate e endDate.");
            }
            return;
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Os parâmetros startDate e endDate devem ser informados juntos.");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("O endDate não pode ser anterior ao startDate.");
        }
    }

    // Lê o corpo JSON da requisição e converte para o tipo informado.
    // Se required for false e o corpo vier vazio, devolve um objeto novo (todos os campos null).
    private <T> T parseRequestBody(IHTTPSession session, Class<T> type, boolean required)
            throws IOException, NanoHTTPD.ResponseException {

        Map<String, String> files = new HashMap<>();
        session.parseBody(files);
        String body = files.get("postData");

        if (body == null || body.trim().isEmpty()) {
            if (required) {
                throw new IllegalArgumentException("O corpo da requisição não pode estar vazio.");
            }
            return newEmptyInstance(type);
        }

        T request = gson.fromJson(body, type);

        if (request == null) {
            throw new IllegalArgumentException("Corpo da requisição inválido ou vazio.");
        }

        return request;
    }

    private <T> T newEmptyInstance(Class<T> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("O DTO " + type.getSimpleName() + " precisa de um construtor vazio.", e);
        }
    }

    private Response jsonResponse(Status status, Object body) {
        return NanoHTTPD.newFixedLengthResponse(status, "application/json; charset=UTF-8", gson.toJson(body));
    }

    private Response errorResponse(Status status, String message) {
        String safeMessage = message != null ? message : "Erro ao processar a requisição.";
        return jsonResponse(status, Map.of("error", safeMessage));
    }
}
