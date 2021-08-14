package ru.sendel.sjctaskschecker.telegram;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
class TelegramMessageService {

    public SendMessage onUpdateReceived(@NonNull Update update) {
        return new SendMessage();
    }
}
