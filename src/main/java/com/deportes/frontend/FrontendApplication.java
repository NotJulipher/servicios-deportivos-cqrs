package com.deportes.frontend;

import com.deportes.frontend.ui.MenuPrincipal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication
public class FrontendApplication {

    private static ConfigurableApplicationContext commandContext;
    private static ConfigurableApplicationContext queryContext;

    public static void main(String[] args) {
        try {
            // Configurar Look and Feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            System.out.println("=================================================");
            System.out.println("    INICIANDO SISTEMA CQRS - SERVICIOS DEPORTIVOS");
            System.out.println("=================================================\n");

            // Iniciar Command Service en un hilo separado
            Thread commandThread = new Thread(() -> {
                try {
                    System.out.println("🚀 Iniciando Command Service (Puerto 8080)...");
                    SpringApplication commandApp = new SpringApplication(com.deportes.command.CommandApplication.class);
                    commandApp.setAdditionalProfiles("command");
                    commandContext = commandApp.run(
                            "--server.port=8080",
                            "--spring.profiles.active=command"
                    );
                    System.out.println("✅ Command Service iniciado correctamente\n");
                } catch (Exception e) {
                    System.err.println("❌ Error al iniciar Command Service: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            commandThread.setDaemon(false);
            commandThread.start();

            // Esperar 10 segundos antes de iniciar Query Service
            System.out.println("⏳ Esperando inicialización de Command Service...");
            Thread.sleep(10000);

            // Iniciar Query Service en un hilo separado
            Thread queryThread = new Thread(() -> {
                try {
                    System.out.println("🚀 Iniciando Query Service (Puerto 8081)...");
                    SpringApplication queryApp = new SpringApplication(com.deportes.query.QueryApplication.class);
                    queryApp.setAdditionalProfiles("query");
                    queryContext = queryApp.run(
                            "--server.port=8081",
                            "--spring.profiles.active=query"
                    );
                    System.out.println("✅ Query Service iniciado correctamente\n");
                } catch (Exception e) {
                    System.err.println("❌ Error al iniciar Query Service: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            queryThread.setDaemon(false);
            queryThread.start();

            // Esperar a que ambos servicios estén listos
            System.out.println("⏳ Esperando inicialización de Query Service...");
            Thread.sleep(10000);

            // Iniciar interfaz gráfica
            SwingUtilities.invokeLater(() -> {
                System.out.println("🎨 Iniciando Interfaz Gráfica...");
                System.out.println("=================================================\n");
                MenuPrincipal menu = new MenuPrincipal();
                menu.setVisible(true);
            });

        } catch (Exception e) {
            System.err.println("❌ Error al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void shutdown() {
        System.out.println("\n🛑 Cerrando servicios...");
        if (queryContext != null) {
            queryContext.close();
        }
        if (commandContext != null) {
            commandContext.close();
        }
        System.out.println("✅ Servicios cerrados correctamente");
        System.exit(0);
    }
}