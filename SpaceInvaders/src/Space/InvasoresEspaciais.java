package Space;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.imageio.ImageIO;

public class InvasoresEspaciais extends JPanel implements ActionListener, KeyListener {

    public abstract class ObjetoDoJogo {
        protected int posX, posY, largura, altura;
        protected boolean estaVivo;

        public ObjetoDoJogo(int posX, int posY, int largura, int altura) {
            this.posX = posX;
            this.posY = posY;
            this.largura = largura;
            this.altura = altura;
            this.estaVivo = true;
        }

        public abstract void mover() throws Exception;
        public abstract void desenhar(Graphics g);
        public Rectangle getLimites() {
            return new Rectangle(posX, posY, largura, altura);
        }
        public boolean estaVivo() {
            return estaVivo;
        }
    }

    public class Jogador extends ObjetoDoJogo {
        private int deslocamentoX;
        private Image imagemJogador;
        private int vidas;

        public Jogador(int posX, int posY, int largura, int altura) {
            super(posX, posY, largura, altura);
            this.vidas = 3;
            try {
                imagemJogador = ImageIO.read(getClass().getResource("/imagens/nave.png"));
            } catch (IOException e) {
                System.out.println("Erro ao carregar a imagem do jogador. Usando retângulo padrão.");
                imagemJogador = null;
            }
        }

        @Override
        public void desenhar(Graphics g) {
            if (imagemJogador != null) {
                g.drawImage(imagemJogador, posX, posY, largura, altura, null);
            } else {
                g.setColor(Color.BLUE);
                g.fillRect(posX, posY, largura, altura);
            }
        }
        
        @Override
        public void mover() throws Exception {
            posX += deslocamentoX;
            if (posX < 0) posX = 0;
            if (posX > getWidth() - largura) posX = getWidth() - largura;
            repaint();
        }

        public void teclaPressionada(KeyEvent e) {
            int tecla = e.getKeyCode();
            if (tecla == KeyEvent.VK_LEFT) {
                deslocamentoX = -5;
            } else if (tecla == KeyEvent.VK_RIGHT) {
                deslocamentoX = 5;
            }
        }

        public void teclaLiberada(KeyEvent e) {
            int tecla = e.getKeyCode();
            if (tecla == KeyEvent.VK_LEFT || tecla == KeyEvent.VK_RIGHT) {
                deslocamentoX = 0;
            }
        }

        public Tiro atirar() {
            return new Tiro(posX + largura / 2 - 2, posY - 10, 5, 10, -5); // Tiro do jogador
        }

        public int getVidas() {
            return vidas;
        }

        public void perderVida() {
            vidas--;
            if (vidas <= 0) {
                estaVivo = false;
            }
        }
    }

    public class Invasor extends ObjetoDoJogo {
        private int deslocamentoX = 1;
        private Image imagemInvasor;
        private Random random = new Random();

        public Invasor(int posX, int posY, int largura, int altura) {
            super(posX, posY, largura, altura);
            try {
                imagemInvasor = ImageIO.read(getClass().getResource("/imagens/nave inimiga.png"));
            } catch (IOException e) {
                System.out.println("Erro ao carregar a imagem do invasor: " + e.getMessage());
            }
        }

        @Override
        public void desenhar(Graphics g) {
            if (estaVivo && imagemInvasor != null) {
                g.drawImage(imagemInvasor, posX, posY, largura, altura, null);
            } else if (estaVivo) {
                g.setColor(Color.RED);
                g.fillRect(posX, posY, largura, altura);
            }
        }
        
        @Override
        public void mover() throws Exception {
            posX += deslocamentoX;
            
            if (posX < 0) {
                posX = 0; // Limita a posição mínima a 0
                deslocamentoX = -deslocamentoX; // Inverte a direção
                posY += 10;
            } else if (posX > getWidth() - largura) {
            	posX = getWidth() - largura;
                deslocamentoX = -deslocamentoX;
                posY += 10; // Desce um nível quando atinge as bordas
            }
            
            if(random.nextDouble() < 0.003) {
            	deslocamentoX = -deslocamentoX;
            }
        }

        public void morrer() {
            this.estaVivo = false;
        }

        public boolean podeAtirar() {
            return estaVivo; // Invasor só pode atirar se estiver vivo
        }

        public Tiro atirar() {
            if (podeAtirar()) {
                return new Tiro(posX + largura / 2 - 2, posY + altura, 5, 10, 5); // Tiro do invasor
            }
            return null;
        }
    }

    public class InvasorRapido extends Invasor {
        public InvasorRapido(int posX, int posY, int largura, int altura) {
            super(posX, posY, largura, altura);
        }

        @Override
        public Tiro atirar() {
            if (podeAtirar()) {
                return new Tiro(posX + largura / 2 - 2, posY + altura, 5, 10, 3); // Tiro mais rápido
            }
            return null;
        }
    }

    public class Tiro extends ObjetoDoJogo {
        private int deslocamentoY;

        public Tiro(int posX, int posY, int largura, int altura, int deslocamentoY) {
            super(posX, posY, largura, altura);
            this.deslocamentoY = deslocamentoY;
        }

        @Override
        public void mover() throws Exception {
            posY += deslocamentoY;
            if (posY < 0 || posY > 600) {
                estaVivo = false;
            }
        }

        @Override
        public void desenhar(Graphics g) {
            if (estaVivo) {
                g.setColor(Color.YELLOW);
                g.fillRect(posX, posY, largura, altura);
            }
        }
    }

    private Jogador jogador;
    private ArrayList<Invasor> invasores;
    private ArrayList<Tiro> tiros;
    private ArrayList<Tiro> tirosInvasores;
    private Timer temporizador;
    private Random random = new Random();
    private int nivel = 1;
    private boolean mudandoDeFase = false;

    public InvasoresEspaciais() {
        setFocusable(true);
        setBackground(Color.BLACK);
        jogador = new Jogador(375, 500, 30, 30);
        invasores = new ArrayList<>();
        tiros = new ArrayList<>();
        tirosInvasores = new ArrayList<>();
        addKeyListener(this);
        criarInvasores(nivel);

        temporizador = new Timer(10, this);
        temporizador.start();
    }

    private void criarInvasores(int nivel) {
        invasores.clear();
        for (int i = 0; i < 5 + nivel; i++) {
            invasores.add(new Invasor(50 + i * 60, 50 + (nivel - 1) * 20, 30, 30)); // Aumenta a Y para cada nível
        }
        if (nivel >= 3 || nivel == 4) {
        	invasores.add(new Invasor(50 + (5 + nivel) * 60, 50 + (nivel - 1) * 20, 30, 30));
        	invasores.add(new Invasor(50 + (6 + nivel) * 60, 50 + (nivel - 1) * 20, 30, 30));
        	
            invasores.add(new InvasorRapido(50 + (7 + nivel) * 60, 50 + (nivel - 1) * 20, 30, 30)); // Invasor rápido
        }
        if (nivel >= 5 || nivel <= 7) {
        	invasores.add(new Invasor(50 + (8 + nivel) * 60, 50 + (nivel - 1) * 20, 30, 30));
        	invasores.add(new Invasor(50 + (9 + nivel) * 60, 50 + (nivel - 1) * 20, 30, 30));
        	
            invasores.add(new InvasorRapido(50 + (10 + nivel) * 60, 50 + (nivel - 1) * 20, 30, 30)); // Invasor rápido
            invasores.add(new InvasorRapido(50 + (11 + nivel) * 60, 50 + (nivel - 1) * 20, 30, 30)); // Invasor rápido
        }
        if (nivel >= 8) {
        	invasores.add(new Invasor(50 + (12 + nivel) * 60, 50 + (nivel - 1) * 20, 30, 30));
        	invasores.add(new Invasor(50 + (13 + nivel) * 60, 50 + (nivel - 1) * 20, 30, 30));
        	invasores.add(new Invasor(50 + (14 + nivel) * 60, 50 + (nivel - 1) * 20, 30, 30));
        	
            invasores.add(new InvasorRapido(50 + (15 + nivel) * 60, 50 + (nivel - 1) * 20, 30, 30)); // Invasor rápido
            invasores.add(new InvasorRapido(50 + (16 + nivel) * 60, 50 + (nivel - 1) * 20, 30, 30)); // Invasor rápido
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        jogador.desenhar(g);
        for (Invasor invasor : invasores) {
            invasor.desenhar(g);
        }
        for (Tiro tiro : tiros) {
            tiro.desenhar(g);
        }
        for (Tiro tiroInvasor : tirosInvasores) {
            tiroInvasor.desenhar(g);
        }

        g.setColor(Color.WHITE);
        g.drawString("Pressione ESC para sair.", 1400, 20);
        g.drawString("Vidas: " + jogador.getVidas(), 10, 20); 
        g.drawString("Nível: " + nivel, 700, 20);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (mudandoDeFase) {
                return; // Não executa nada enquanto muda de fase
            }
            jogador.mover();
            for (Invasor invasor : invasores) {
                invasor.mover();
                if (invasor.podeAtirar() && random.nextDouble() < 0.01) {
                    Tiro tiroInvasor = invasor.atirar();
                    if (tiroInvasor != null) {
                        tirosInvasores.add(tiroInvasor);
                    }
                }
            }
            for (Tiro tiro : tiros) {
                tiro.mover();
            }
            for (Tiro tiroInvasor : tirosInvasores) {
                tiroInvasor.mover();
            }
            verificarColisoes();
            repaint();
            if (invasores.isEmpty()) {
                nivel++;
                mudandoDeFase = true;
                Timer timer = new Timer(2000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mudandoDeFase = false;
                        criarInvasores(nivel);
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void verificarColisoes() {
        Iterator<Tiro> iteradorDeTiros = tiros.iterator();
        while (iteradorDeTiros.hasNext()) {
            Tiro tiro = iteradorDeTiros.next();
            if (!tiro.estaVivo()) {
                iteradorDeTiros.remove();
                continue;
            }
            for (Iterator<Invasor> iteradorDeInvasores = invasores.iterator(); iteradorDeInvasores.hasNext();) {
                Invasor invasor = iteradorDeInvasores.next();
                if (invasor.estaVivo() && tiro.getLimites().intersects(invasor.getLimites())) {
                    invasor.morrer();
                    iteradorDeInvasores.remove();
                    iteradorDeTiros.remove();
                    break; // Saia do loop interno para evitar ConcurrentModificationException
                }
            }
        }

        Iterator<Tiro> iteradorDeTirosInvasores = tirosInvasores.iterator();
        while (iteradorDeTirosInvasores.hasNext()) {
            Tiro tiroInvasor = iteradorDeTirosInvasores.next();
            if (!tiroInvasor.estaVivo()) {
                iteradorDeTirosInvasores.remove();
                continue;
            }
            if (tiroInvasor.getLimites().intersects(jogador.getLimites())) {
                jogador.perderVida();
                iteradorDeTirosInvasores.remove();
                if (!jogador.estaVivo()) {
                    JOptionPane.showMessageDialog(this, "Game Over!");
                    System.exit(0);
                }
            }
        }
    }
    
	private boolean deveAtirar = true;

    @Override
    public void keyPressed(KeyEvent e) {
        jogador.teclaPressionada(e);
        
        // Verifica se a tecla pressionada é ESC
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            int resposta = JOptionPane.showConfirmDialog(null, "Deseja realmente sair?", "Confirmação", JOptionPane.YES_NO_OPTION);
            if (resposta == JOptionPane.YES_OPTION) {
                System.exit(0);  // Encerra o programa
            }
        }
        
        if (e.getKeyCode() == KeyEvent.VK_SPACE && deveAtirar) {
            Tiro tiro = jogador.atirar();
            if (tiro != null) {
                tiros.add(tiro);
                deveAtirar = false;
            }
        } 
    }

    @Override
    public void keyReleased(KeyEvent e) {
        jogador.teclaLiberada(e);
        
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            deveAtirar = true;
        }
    }
    
    public static void main(String[] args) {
        // Cria a janela principal do jogo
        JFrame janela = new JFrame("Invasores Espaciais");

        // Adiciona a tela inicial ao invés de iniciar diretamente o jogo
        TelaInicial telaInicial = new TelaInicial(janela);
        janela.add(telaInicial);

        // Define a operação padrão ao fechar a janela
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Ajusta para tela cheia, se necessário
        setFullScreen(janela);

        // Mostra a janela após toda a configuração
        janela.setVisible(true);
    }

    private static void setFullScreen(JFrame janela) {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            janela.setUndecorated(true);
            gd.setFullScreenWindow(janela);
        } else {
            janela.setSize(800, 600);
            janela.setVisible(true);
        }
    }


}
