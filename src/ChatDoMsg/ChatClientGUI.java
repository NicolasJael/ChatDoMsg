package ChatDoMsg;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatClientGUI extends JFrame {
    private static final String IP_SERVIDOR = "192.168.0.46";
    private static final int PORTA_SERVIDOR = 5556;
    private PrintWriter out;
    private JTextArea areaChat;
    private JLabel rotuloImagemPerfil;
    private File arquivoHistoricoChat;
    private String nomeUsuario;
    private ArrayList<String> membrosGrupo;

    public ChatClientGUI() {
        configurarJanela();
        adicionarComponentes();
        adicionarListeners();
        iniciarConexaoServidor();
    }

    private void configurarJanela() {
        setTitle("Chat dos mongois");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);
    }

    private void adicionarComponentes() {
        JPanel painelChat = new JPanel(new BorderLayout());
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        JScrollPane painelRolagem = new JScrollPane(areaChat);
        painelChat.add(painelRolagem, BorderLayout.CENTER);
        add(painelChat, BorderLayout.CENTER);

        JPanel painelEntrada = new JPanel(new BorderLayout());
        JTextField entradaMensagem = new JTextField();
        JButton botaoEnviar = new JButton("Mandar");
        JButton botaoUploadImagem = new JButton("Subir Imagem");
        JButton botaoCriarGrupo = new JButton("Criar Grupo");
        painelEntrada.add(entradaMensagem, BorderLayout.CENTER);
        painelEntrada.add(botaoEnviar, BorderLayout.EAST);
        painelEntrada.add(botaoUploadImagem, BorderLayout.WEST);
        painelEntrada.add(botaoCriarGrupo, BorderLayout.NORTH);
        add(painelEntrada, BorderLayout.SOUTH);

        rotuloImagemPerfil = new JLabel();
        rotuloImagemPerfil.setHorizontalAlignment(SwingConstants.CENTER);
        add(rotuloImagemPerfil, BorderLayout.WEST);
    }

    private void adicionarListeners() {
        botaoUploadImagem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selecionarImagem();
            }
        });

        botaoEnviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensagem();
            }
        });

        botaoCriarGrupo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                criarGrupo();
            }
        });
    }

    private void iniciarConexaoServidor() {
        arquivoHistoricoChat = new File("historico.txt");
        carregarHistoricoChat();

        try {
            Socket socket = new Socket(IP_SERVIDOR, PORTA_SERVIDOR);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            Thread threadRecebimento = new Thread(() -> {
                String msg;
                try {
                    while ((msg = entrada.readLine()) != null) {
                        areaChat.append("Servidor: " + msg + "\n");
                        salvarMensagemNoHistorico("Servidor: " + msg);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            threadRecebimento.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void selecionarImagem() {
        JFileChooser escolherArquivo = new JFileChooser();
        int resultado = escolherArquivo.showOpenDialog(ChatClientGUI.this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivoSelecionado = escolherArquivo.getSelectedFile();
            try {
                BufferedImage imagem = ImageIO.read(arquivoSelecionado);
                exibirImagemPerfil(imagem);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void exibirImagemPerfil(BufferedImage imagem) {
        rotuloImagemPerfil.setIcon(new ImageIcon(imagem.getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
    }

    private void enviarMensagem() {
        String mensagem = entradaMensagem.getText().trim();
        if (!mensagem.isEmpty()) {
            out.println(mensagem);
            entradaMensagem.setText("");
            salvarMensagemNoHistorico("Voce: " + mensagem);
        }
    }

    private void criarGrupo() {
        String nomeGrupo = JOptionPane.showInputDialog(ChatClientGUI.this, "Digite o nome do grupo:");
        if (nomeGrupo != null && !nomeGrupo.isEmpty()) {
            membrosGrupo = new ArrayList<>();
            membrosGrupo.add(nomeUsuario);
            JOptionPane.showMessageDialog(ChatClientGUI.this, "Grupo '" + nomeGrupo + "' criado ;)");
        }
    }

    private void carregarHistoricoChat() {
        try (BufferedReader leitor = new BufferedReader(new FileReader(arquivoHistoricoChat))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                areaChat.append(linha + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void salvarMensagemNoHistorico(String mensagem) {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(arquivoHistoricoChat, true))) {
            SimpleDateFormat formatoData = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String timestamp = formatoData.format(new Date());
            escritor.write("[" + timestamp + "] " + mensagem + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String nomeUsuario = JOptionPane.showInputDialog("Digite seu nome de usuario");
        SwingUtilities.invokeLater(() -> {
            ChatClientGUI clienteGUI = new ChatClientGUI();
            clienteGUI.setNomeUsuario(nomeUsuario);
            clienteGUI.setVisible(true);
        });
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }
}