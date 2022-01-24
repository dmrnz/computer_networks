package phrases;

public enum Phrases {
    SERVER("Сервер"),
    NAME_IS_TAKEN("Это имя уже занято, пожалуйста, введите другое."),
    WELCOME("К нам подключился "),
    USER_DISCONNECT(" вышел из чата."),
    SERVER_CLOSED("Сервер прекратил работу."),
    BAD_COMMANDS("Неверная команда, для помощи введите /help"),
    COMMANDS("/exit - прекратить работу сервера\n/users - вывести список подключившихся пользователей"),

    ENTER_NAME("Введите имя: "),
    FILE_NOT_EXIST("Такого файла не существует, пожалуйста, введите корректный путь до файла."),
    SENT_FILE("Отправлен файл: "),
    CREATE_DIRECTORY("Директория для файлов успешно создана: "),
    FILE_RECEIVED("Файл успешно получен: "),
    FILE_OVERWRITTEN("Файл успешно получен и перезаписан: "),

    EXIT("/exit"),
    HELP("/help"),
    USERS("/users"),
    FILE("-file "),
    DOWNLOAD_DIRECTORY("Desktop");

    private final String phrase;

    Phrases(String phrase) {
        this.phrase = phrase;
    }

    public String getPhrase() {
        return phrase;
    }

    public static Phrases fromString(String text) {
        for (Phrases phrase : Phrases.values()) {
            if (phrase.phrase.equalsIgnoreCase(text)) {
                return phrase;
            }
        }
        throw new IllegalArgumentException(BAD_COMMANDS.getPhrase());
    }
}