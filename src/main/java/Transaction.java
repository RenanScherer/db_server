import java.io.IOException;

public class Transaction {
    private Integer transactionNum;
    private String client;
    private String logfile;
    private String datafile;

    public Transaction(Integer transactionNum, String client, String logfile, String datafile) {
        this.transactionNum = transactionNum;
        this.client = client;
        this.logfile = logfile;
        this.datafile = datafile;
    }

    public Boolean createTransaction() {
        String parameters = "\"" + this.client + "\" \"" + this.transactionNum + "\" \"" + this.datafile + "\" \"" + this.logfile + "\"";
        try {
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "cmd", "/k", "java -jar " + parameters});
        } catch (IOException e) {
            System.out.println("Erro ao criar nova transacao!");
            e.printStackTrace();
        }
        return true;
    }
}
