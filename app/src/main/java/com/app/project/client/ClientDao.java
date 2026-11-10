package com.app.project.client;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Transaction;
import java.util.List;

@Dao
public interface ClientDao {

    // Insere um novo cliente no banco de dados.
    // Retorna o ID do cliente inserido.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertClient(Client client);

    // Insere uma lista de clientes.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertClients(List<Client> clients);

    // Atualiza as informações de um cliente existente.
    @Update
    int updateClient(Client client);

    // Remove um cliente do banco de dados.
    @Query("DELETE FROM clients WHERE clientId = :id")
    int deleteClient(long id);

    // Busca todos os clientes cadastrados sem as vendas associadas.
    @Query("SELECT * FROM clients ORDER BY name ASC")
    List<Client> getAllClients();

    // Busca um cliente específico pelo seu ID.
    @Query("SELECT * FROM clients WHERE clientId = :id")
    Client getClientById(long id);

    @Query("SELECT * FROM clients WHERE name LIKE '%' || :name || '%' ORDER BY name ASC, clientId ASC")
    List<Client> getClientsByName(String name);

    @Query("SELECT * FROM clients WHERE phone = :phone LIMIT 1")
    Client getClientByPhone(String phone);

    @Query("SELECT * FROM clients ORDER BY name ASC, clientId ASC LIMIT :size OFFSET :offset")
    List<Client> getClientsPage(int size, int offset);

    // Busca todos os clientes juntamente com suas respectivas vendas.
    // A anotação @Transaction é obrigatória, pois o Room executará múltiplas consultas para montar o POJO.
    @Transaction
    @Query("SELECT * FROM clients")
    List<ClientWithSales> getClientsWithSales();
    
    // Busca um cliente específico pelo ID, trazendo também todas as suas vendas.
    @Transaction
    @Query("SELECT * FROM clients WHERE clientId = :id")
    ClientWithSales getClientWithSalesById(long id);

    @Query("SELECT * FROM clients WHERE name LIKE '%' || :name || '%' ORDER BY name ASC, clientId ASC LIMIT :size OFFSET :offset")
    List<Client> getClientsByNamePage(String name, int size, int offset);
}