import java.io.*;
import java.util.Scanner;

public class MainApplication {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        String client = "C:/Users/Renan/Desktop/banco/db-client-project/build/libs/server-2-0.0.1-SNAPSHOT.jar";
        String logfile = "C:/Users/Renan/Desktop/banco/logfile.txt";
        String datafile = "C:/Users/Renan/Desktop/banco/datafile.txt";

//        String path = "C:/Users/Renan/Desktop/banco/file.txt";

        Integer transactionNum = new Integer(1);

        Integer option;

        while (true) {
            System.out.println("Escolha a acao a ser executada:");
            System.out.println("    1: Visualizar dados");
            System.out.println("    2: Nova transação");
            System.out.println("    3: Checkpoint");
            System.out.println("    4: Encerrar");
            option = Integer.parseInt(input.nextLine());

            switch (option) {
                case 1:
                    readFile(datafile);
                    break;
                case 2:
                    Transaction transaction = new Transaction(transactionNum, client, logfile, datafile);
                    transaction.createTransaction();
                    transactionNum++;
                    break;
                case 3:
                    checkpoint(transactionNum, logfile, datafile);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("\nOpcao invalida\n");
            }
        }
    }

    public static void readFile(String path) {
        String line;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo " + path + " nao encontrado");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo");
            e.printStackTrace();
        }
    }

    public static int ultimaOcorrencia(String file, String palavra) {
        String linha;
        Integer cont = 0;
        Integer ultimaOcorrencia = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((linha = reader.readLine()) != null) {
                cont++;
                if (linha.equalsIgnoreCase(palavra)) {
                    ultimaOcorrencia = cont;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo " + file + " nao encontrado");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo");
            e.printStackTrace();
        }
        return ultimaOcorrencia;
    }

    public static void transcreverLogEmDatafile (Integer transaction, String logfile, String datafile) {
        String linha;
        Aluno aluno;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(logfile));
            while ((linha = reader.readLine()) != null) {
                if (linha.contains(transaction + " transaction: insert")) {
                    aluno = new Aluno();
                    aluno.setCodigo(Integer.parseInt(reader.readLine()));
                    aluno.setNome(reader.readLine());
                    aluno.setNota(Double.parseDouble(reader.readLine()));
                    insert(aluno, datafile);
                }
                if (linha.contains(transaction + " transaction: update")) {
                    aluno = new Aluno();
                    Integer codigo = Integer.parseInt(reader.readLine());
                    aluno.setCodigo(Integer.parseInt(reader.readLine().substring(8)));
                    aluno.setNome(reader.readLine().substring(6));
                    aluno.setNota(Double.parseDouble(reader.readLine().substring(6)));
                    update(datafile, aluno, codigo);
                }
                if (linha.contains(transaction + " transaction: delete")) {
                    Integer codigo = Integer.parseInt(reader.readLine());
                    delete(datafile, codigo);
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo " + logfile + " nao encontrado");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo");
            e.printStackTrace();
        }
    }

    public static void checkpoint(Integer transaction, String logfile, String datafile) {
        for (int i = 1; i <= transaction; i++) {
            if ((ultimaOcorrencia(logfile, "CHECKPOINT")) < (ultimaOcorrencia(logfile, i + "transaction: COMMIT")) ||
                (ultimaOcorrencia(logfile, "CHECKPOINT")) < (ultimaOcorrencia(logfile, i + "transaction: ROLLBACK"))) {
                transcreverLogEmDatafile(i, logfile, datafile);
            }
        }

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(logfile, true));
            writer.println("\nCHECKPOINT");
            writer.close();
        } catch (IOException e) {
            System.out.println("\nERRO: ao fazer checkpoint\n");
            e.printStackTrace();
        }
    }

    public static void insert(Aluno aluno, String file) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(file, true));
        Scanner in = new Scanner(System.in);
        writer.println();
        writer.println("codigo: " + aluno.getCodigo());
        writer.println("nome: " + aluno.getNome());
        writer.println("nota: " + aluno.getNota());
        writer.close();
    }

    public static void update(String file, Aluno novo, Integer codigo) throws IOException {
        String arquivoTmp = "temp";
        String linha;
        PrintWriter writer = new PrintWriter(new FileWriter(arquivoTmp));
        BufferedReader reader;

        Boolean codigoNaoExiste = true;
        if (novo.getCodigo() != codigo) {
            reader = new BufferedReader(new FileReader(file));
            while ((linha = reader.readLine()) != null) {
                if (linha.contains("codigo: " + codigo)) {
                    codigoNaoExiste = false;
                }
            }
            reader.close();
        }
        if (codigoNaoExiste == true) {
            System.out.println("\nERRO: O codigo nao existe!\n");
            return;
        }

        if (novo.getCodigo() != codigo) {
            reader = new BufferedReader(new FileReader(file));
            while ((linha = reader.readLine()) != null) {
                if (linha.contains("codigo: " + novo.getCodigo())) {
                    System.out.println("\nERRO: O codigo ja existe!\n");
                    reader.close();
                    return;
                }
            }
            reader.close();
        }

        reader = new BufferedReader(new FileReader(file));
        while ((linha = reader.readLine()) != null) {
            if (linha.contains("codigo: " + codigo)) {
                if (novo.getCodigo() != codigo) {
                    linha = linha.replace("codigo: " + codigo, "codigo: " + novo.getCodigo());
                    System.out.println(linha);
                    writer.println(linha);
                }
                if (((linha = reader.readLine()) != null) && (("nome: " + novo.getNome()) != linha)) {
                    System.out.println(linha);
                    writer.println(linha);
                }
                if (((linha = reader.readLine()) != null) && (("nota: " + novo.getNota()) != linha)) {
                    System.out.println(linha);
                    writer.println(linha);
                }
                if ((linha = reader.readLine()) == null) {
                    break;
                }
            }
            writer.println(linha);
        }

        writer.close();
        reader.close();

        new File(file).delete();
        new File(arquivoTmp).renameTo(new File(file));
    }

    public static void delete(String file, Integer codigo) throws IOException {
        String arquivoTmp = "temp";
        String linha;
        PrintWriter writer = new PrintWriter(new FileWriter(arquivoTmp));
        BufferedReader reader;

        Boolean deletou = false;
        reader = new BufferedReader(new FileReader(file));
        while ((linha = reader.readLine()) != null) {
            if (linha.contains("codigo: " + codigo)) {
                System.out.println(linha);
                if (((linha = reader.readLine()) != null)) {
                    System.out.println(linha);
                }
                if (((linha = reader.readLine()) != null)) {
                    System.out.println(linha);
                }
                deletou = true;
                if ((linha = reader.readLine()) == null) {
                    break;
                }
            }
            writer.println(linha);
        }
        if (deletou) {
            System.out.println("Registro " + codigo + " deletado");
        } else {
            System.out.println("ERRO: Nao foi possivel deletar o registro");
        }

        writer.close();
        reader.close();

        new File(file).delete();
        new File(arquivoTmp).renameTo(new File(file));
    }

}