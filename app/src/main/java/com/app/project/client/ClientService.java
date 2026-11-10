package com.app.project.client;

import java.util.ArrayList;
import java.util.List;

public class ClientService{


    private ClientRepository clientRepository;


    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }



    public ClientDto getClientById(long id) {
        
        Client client = this.clientRepository.findById(id);
        
        return client != null ? toDto(client) : null;
    }

    public List<ClientDto> getClientByName(String name) {
        
        List<Client> clients = this.clientRepository.findByName(name);
        
        if(clients == null) return null;
        
        List<ClientDto> clientsDto = new ArrayList<>(clients.size());

        for (Client client : clients) {
            clientsDto.add(toDto(client));
        }


        return clientsDto;
    
    }

    public List<ClientDto> getAllClients() {
        
        List<Client> clients = this.clientRepository.findAll();
        List<ClientDto> result = new ArrayList<>(clients.size());

        for (Client client : clients) {
            result.add(toDto(client));
        }

        return result;
    }

    public List<ClientDto> getClientsPaginated(String name, String phone, int page, int size) {
        
        List<Client> clients = new java.util.ArrayList<>();

        if (phone != null) {
            
            Client client = this.clientRepository.findByPhone(phone);
            if (client != null) clients.add(client);
        
        } else if (name != null) {
            clients = this.clientRepository.findByNamePage(name, page, size);
        
        } else {
            clients = this.clientRepository.findPage(page, size);
        
        }

        List<ClientDto> result = new java.util.ArrayList<>(clients.size());
        for (Client client : clients) {
            result.add(toDto(client));
        }

        return result;
}

    public ClientDto getClientByPhone(String phone) {
        
        Client client = this.clientRepository.findByPhone(phone);
        
        return client != null ? toDto(client) : null;
    
    }

    public ClientWithSalesDto getClientWithSalesById(long id) {
        ClientWithSales pojo = this.clientRepository.findWithSalesById(id);
        
        if (pojo == null) return null;

        ClientWithSalesDto dto = new ClientWithSalesDto();
        dto.setClient(toDto(pojo.client));
        
        // Se você possuir um SaleDto no futuro, converta a lista de vendas aqui.
        // Por enquanto, repassamos a lista da entidade Sale mapeada pelo Room.
        dto.setSales(pojo.sales);
        
        return dto;
    }


    
    synchronized public ClientDto createClient(ClientDto clientDTO) {
        
        Client client = toClient(clientDTO);
        client.setId(0);

        Client created = this.clientRepository.createClient(client);

        return created != null ? toDto(created) : null;
}

    public ClientDto updateClient(long clientId, ClientDto dto) {
        
        Client existing = this.clientRepository.findById(clientId);
        
        if (existing == null) return null;

        if (dto.getName() != null) existing.setName(dto.getName());
        
        if (dto.getPhone() != null) existing.setPhone(dto.getPhone());

        Client updated = clientRepository.updateClient(existing);
        
        return updated != null ? toDto(updated) : null;
    }

    public boolean deleteClient(long id){

        boolean removed = this.clientRepository.deleteClient(id);

        return removed;
    
    }

    
    private ClientDto toDto(Client client) {
        
        ClientDto dto = new ClientDto();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setPhone(client.getPhone());
        return dto;
    }

    private Client toClient(ClientDto dto) {
        
        Client newClient = new Client();

        if (dto.getId() != null) {
            
            newClient.setId(dto.getId());
        }

        newClient.setName(dto.getName());
        newClient.setPhone(dto.getPhone());
        return newClient;
    }
















}