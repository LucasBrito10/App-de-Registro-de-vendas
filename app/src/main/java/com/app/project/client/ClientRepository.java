package com.app.project.client;

import com.app.project.client.ClientWithSales; 
import java.util.List;

public class ClientRepository {

    private final ClientDao clientDao;

    public ClientRepository(ClientDao clientDao) {
        this.clientDao = clientDao;
    }

    public Client findById(long id) {
        return clientDao.getClientById(id);
    }

    public List<Client> findByName(String name) {
        return clientDao.getClientsByName(name);
    }

    public Client findByPhone(String phone) {
        return clientDao.getClientByPhone(phone);
    }

    public List<Client> findAll() {
        return clientDao.getAllClients();
    }

    public List<Client> findPage(int page, int size) {
        return clientDao.getClientsPage(size, (page - 1) * size);
    }

    
    public List<Client> findByNamePage(String name, int page, int size) {
        return clientDao.getClientsByNamePage(name, size, (page - 1) * size);
    }

    public ClientWithSales findWithSalesById(long id) {
        return clientDao.getClientWithSalesById(id);
    }

    public Client createClient(Client client) {
        
        long id = clientDao.insertClient(client);

        if (id == -1) return null;

        client.setId(id);
        
        return client;
    }

    public Client updateClient(Client client) {
    
        int rows = clientDao.updateClient(client);
    
        return rows > 0 ? client : null;
    }

    public boolean deleteClient(long id) {
        
        return clientDao.deleteClient(id) > 0;
    }
}