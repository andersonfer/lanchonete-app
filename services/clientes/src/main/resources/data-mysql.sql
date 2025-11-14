INSERT INTO cliente (nome, cpf, email)
VALUES ('João da Silva', '55555555555', 'joao.silva@lanchonete.com') AS novo
ON DUPLICATE KEY UPDATE
    nome = novo.nome,
    email = novo.email;
