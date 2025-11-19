package com.example.spring_integration_sftp.config;

import com.example.spring_integration_sftp.service.FileProcessingService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageHandler;

import java.io.File;
import java.nio.file.*;
import java.util.List;

@Configuration
public class IntegrationConfig {

    @Value("${sftp.poll.interval:5000}")
    private long pollInterval;

    @Value("${sftp.remote.directory:/upload}")
    private String remoteDir;

    @Value("${sftp.local.incoming}")
    private String localIncoming;

    @Value("${sftp.local.processed}")
    private String localProcessed;

    @Value("${sftp.host:localhost}")
    private String sftpHost;

    @Value("${sftp.port:2222}")
    private int sftpPort;

    @Value("${sftp.username:nikhil}")
    private String sftpUser;

    @Value("${sftp.password:password}")
    private String sftpPassword;

    private final FileProcessingService processingService;

    public IntegrationConfig(FileProcessingService processingService) {
        this.processingService = processingService;
        System.out.println("incoming=" + localIncoming);
        System.out.println("processed=" + localProcessed);

    }
    @PostConstruct
    public void init() {
        createLocalDirs();
        System.out.println("incoming=" + localIncoming);
        System.out.println("processed=" + localProcessed);
    }

    private void createLocalDirs() {
        try {
            Path incoming = Paths.get(localIncoming).toAbsolutePath().normalize();
            Path processed = Paths.get(localProcessed).toAbsolutePath().normalize();

            System.out.println("Creating incoming dir: " + incoming);
            System.out.println("Creating processed dir: " + processed);

            Files.createDirectories(incoming);
            Files.createDirectories(processed);

        } catch (Exception e) {
            System.err.println("ERROR WHILE CREATING DIRECTORIES: " + e.getClass().getName());
            System.err.println("MESSAGE: " + e.getMessage());
            throw new RuntimeException("Failed to create directories", e);
        }
    }



    // Create SFTP session factory
    @Bean
    public DefaultSftpSessionFactory sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(sftpHost);
        factory.setPort(sftpPort);
        factory.setUser(sftpUser);
        factory.setPassword(sftpPassword);
        factory.setAllowUnknownKeys(true);
        return factory;
    }

    @Bean
    public SftpInboundFileSynchronizer sftpInboundFileSynchronizer(DefaultSftpSessionFactory sessionFactory) {
        SftpInboundFileSynchronizer sync = new SftpInboundFileSynchronizer(sessionFactory);
        sync.setDeleteRemoteFiles(true);
        sync.setRemoteDirectory(remoteDir);
        sync.setFilter(new SftpSimplePatternFileListFilter("*.csv"));
        return sync;
    }

    @Bean
    public SftpInboundFileSynchronizingMessageSource sftpMessageSource(
            SftpInboundFileSynchronizer synchronizer) {

        SftpInboundFileSynchronizingMessageSource source =
                new SftpInboundFileSynchronizingMessageSource(synchronizer);

        source.setLocalDirectory(new File(localIncoming));
        source.setAutoCreateLocalDirectory(true);
        source.setLocalFilter(new AcceptOnceFileListFilter<>());
        return source;
    }

    // UPDATED HERE — using IntegrationFlow.from() instead of IntegrationFlows.from()
    @Bean
    public IntegrationFlow sftpInboundFlow(SftpInboundFileSynchronizingMessageSource source) {
        return IntegrationFlow
                .from(source, c -> c.poller(Pollers.fixedDelay(pollInterval)))
                .filter(File.class, file -> file.getName().toLowerCase().endsWith(".csv"))
                .handle(fileMessageHandler())
                .get();
    }

    @Bean
    public MessageHandler fileMessageHandler() {
        return message -> {
            try {
                File file = (File) message.getPayload();
                Path path = file.toPath();

                List<?> saved = processingService.processCsvFile(path);

                String newName = file.getName() + "." + System.currentTimeMillis();
                Path target = Paths.get(localProcessed).resolve(newName);

                Files.move(path, target, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Processed: " + file.getName() + " | Rows saved: " + saved.size());

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}