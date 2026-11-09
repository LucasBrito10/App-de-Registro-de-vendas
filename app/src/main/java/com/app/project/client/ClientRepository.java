package com.app.project.client;


public class ClientRepository{

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


    public Client createClient(Client client) {
        return null;
    }

    public Client updateClient(Client client) {
        return null;
    }







}