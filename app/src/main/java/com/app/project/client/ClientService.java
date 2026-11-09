package com.app.project.client;

import java.util.ArrayList;
import java.util.List;

public class ClientService{


    private ClientRepository clientRepository;


    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }



    public ClientDto getClientById(long id) {
        
        Client client = clientRepository.findById(id);
        
        return client != null ? toDto(client) : null;
    }

    public List<ClientDto> getClientByName(String name) {
        
        List<Client> clients = clientRepository.findByName(name);
        
        if(clients == null) return null;
        
        List<Dto> clientsDto = new ArrayList<>(clients.size());

        for (Client client : clients) {
            result.add(toDto(client));
        }


        return clientsDto;
    
    }

    public List<ClientDto> getAllClients() {
        
        List<Client> clients = clientRepository.findAll();
        List<ClientDto> result = new ArrayList<>(clients.size());

        for (Client client : clients) {
            result.add(toDto(client));
        }

        return result;
    }

    public List<ClientDto> getClientsPaginated(int page, int size) {
        
        List<Client> clients = clientRepository.findPage(page, size);
        List<ClientDto> result = new ArrayList<>(clients.size());

        for (Client client : clients) {
            result.add(toDto(client));
        }

        return result;
    }

    public ClientDto getClientByPhone(String phone) {
        
        Client client = clientRepository.findByPhone(phone);
        
        return client != null ? toDto(client) : null;
    
    }


    
    synchronized public ClientDto createClient(ClientDto clientDTO) {
        return null;
        
    }

    synchronized public ClientDto updateClient(ClientDto clientDTO) {
        return null;
    }

    
    private ClientDto toDto(Client client) {
        
        ClientDto dto = new ClientDto();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setPhone(client.getPhone());
        return dto;
    }
















}