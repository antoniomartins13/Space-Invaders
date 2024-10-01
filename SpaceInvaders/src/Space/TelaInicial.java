package Space;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class TelaInicial extends JPanel {
    private JFrame janela;
    private Image fundo;

    public TelaInicial(JFrame janela) {
        this.janela = janela;
        setLayout(new BorderLayout());

        // Carregar a imagem de fundo
        try {
            fundo = ImageIO.read(getClass().getResource("/imagens/spaceinvaders.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Adiciona um KeyListener para detectar teclas pressionadas
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                iniciarJogo(); // Inicia o jogo ao pressionar qualquer tecla
            }
        });

        setFocusable(true); // Permite que o painel receba eventos de teclado
        requestFocusInWindow(); // Solicita o foco para o painel
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (fundo != null) {
            g.drawImage(fundo, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void iniciarJogo() {
        janela.getContentPane().removeAll(); 
        InvasoresEspaciais jogo = new InvasoresEspaciais(); 
        janela.add(jogo); 
        janela.revalidate(); 
        janela.repaint(); 
        jogo.requestFocus(); 
    }
}

