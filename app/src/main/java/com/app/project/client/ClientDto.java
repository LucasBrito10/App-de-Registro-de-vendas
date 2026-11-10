package com.app.project.client;

public class ClientDto {

    
    private Long clientId;
    private String name;
    private String phone;

    // Construtor vazio (obrigatório para bibliotecas de serialização como Gson/Jackson)
    public ClientDto() {
    }

    // Construtor completo
    public ClientDto(Long clientId, String name, String phone) {
        this.clientId = clientId;
        this.name = name;
        this.phone = phone;
    }

    // Getters e Setters
    public Long getId() {
        return clientId;
    }

    public void setId(Long clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}