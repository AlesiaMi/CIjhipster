package com.mycompany.myapp.service.importer;

import com.mycompany.myapp.web.websocket.ImportProgressWebSocketHandler;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ImportProgressService {

    private final ImportProgressWebSocketHandler socket;

    private final ThreadLocal<ActiveImport> currentImport = new ThreadLocal<>();

    public ImportProgressService(ImportProgressWebSocketHandler socket) {
        this.socket = socket;
    }

    public void start(String login, UUID importId, int total) {
        currentImport.set(new ActiveImport(login, importId, total));
    }

    public void reportBatch(int processed) {
        ActiveImport current = currentImport.get();

        if (current != null) {
            send(current.login(), current.importId(), "SAVING", processed, current.total(), "Сохранение записей (в транзакции)");
        }
    }

    public void clear() {
        currentImport.remove();
    }

    public void send(String login, UUID importId, String phase, int processed, int total, String message) {
        if (login == null || importId == null) {
            return;
        }

        int percent = switch (phase) {
            case "COMPLETED" -> 100;
            case "COMMITTING" -> 95;
            case "SAVING" -> total > 0 ? Math.min(95, (int) ((95L * processed) / total)) : 0;
            default -> 0;
        };

        socket.sendToUser(login, new ImportProgressEvent("progress", importId.toString(), phase, processed, total, percent, message));
    }

    public record ImportProgressEvent(String type, String importId, String phase, int processed, int total, int percent, String message) {}

    private record ActiveImport(String login, UUID importId, int total) {}
}
