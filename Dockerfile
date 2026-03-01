services:
  backend:
    build: ./chat-api
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://chat-db:5432/chatdb
      - SPRING_DATASOURCE_USERNAME=user_chat
      - SPRING_DATASOURCE_PASSWORD=password_chat

    networks:
      - app-network

  frontend:
    build: ./chat-front
    ports:
      - "5173:80" # Mapeia a porta 80 do container para 5173 no host (ou use 80:80)
    depends_on:
      - backend
    networks:
      - app-network

networks:
  app-network:
    driver: bridge
