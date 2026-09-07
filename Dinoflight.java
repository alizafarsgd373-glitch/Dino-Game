
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Dinoflight extends JPanel implements ActionListener, KeyListener {

    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image backgroundImg;
    Image dinoImg;
    Image cactusImg;
    Image bombImg;

    // Dino (Dragon)
    int dinoX = boardWidth / 8;
    int dinoY = boardHeight / 2;
    int dinoWidth = 50;
    int dinoHeight = 40;

    class Dino {
        int x = dinoX;
        int y = dinoY;
        int width = dinoWidth;
        int height = dinoHeight;
        Image img;

        Dino(Image img) {
            this.img = img;
        }
    }

    // Obstacles
    int obsX = boardWidth;
    int obsWidth = 64;
    int obsHeight = 64;

    class Obstacle {
        int x = obsX;
        int y;
        int width = obsWidth;
        int height = obsHeight;
        Image img;
        boolean passed = false;

        Obstacle(Image img, int y) {
            this.img = img;
            this.y = y;
        }
    }

    Dino dino;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Obstacle> obstacles;
    Random random = new Random();

    Timer gameLoop;
    Timer placeObstacleTimer;
    boolean gameOver = false;
    double score = 0;

    Dinoflight() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon("nightsky.png").getImage();
        dinoImg = new ImageIcon("dragon.png").getImage();
        cactusImg = new ImageIcon("cactus.png").getImage();
        bombImg = new ImageIcon("bomb.png").getImage();

        dino = new Dino(dinoImg);
        obstacles = new ArrayList<>();

        // Place obstacles timer
        placeObstacleTimer = new Timer(2500, e -> placeObstacles());
        placeObstacleTimer.start();

        // Game loop timer
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    void placeObstacles() {
        // Randomly decide obstacle type
        int type = random.nextInt(2);

        // Random Y within playable area (avoid spawning too high or low)
        int minY = 100;
        int maxY = boardHeight - 200;
        int y = random.nextInt(maxY - minY) + minY;

        // Create single obstacle at random height (not overlapping start)
        obstacles.add(new Obstacle(type == 0 ? cactusImg : bombImg, y));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        // Dino
        g.drawImage(dinoImg, dino.x, dino.y, dino.width, dino.height, null);

        // Obstacles
        for (Obstacle obs : obstacles) {
            g.drawImage(obs.img, obs.x, obs.y, obs.width, obs.height, null);
        }

        // Score
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        if (gameOver) {
            g.drawString("GAME OVER", boardWidth / 2 - 100, boardHeight / 2 - 50);
            g.drawString("Score: " + (int) score, boardWidth / 2 - 70, boardHeight / 2);
        } else {
            g.drawString("Score: " + (int) score, 10, 35); // only one score text now
        }
    }

    public void move() {
        // Dino movement
        velocityY += gravity;
        dino.y += velocityY;
        dino.y = Math.max(dino.y, 0);

        // Obstacles movement
        for (Obstacle obs : obstacles) {
            obs.x += velocityX;

            if (!obs.passed && dino.x > obs.x + obs.width) {
                score += 1; // increase score once per obstacle
                obs.passed = true;
            }

            if (collision(dino, obs)) {
                gameOver = true;
            }
        }

        if (dino.y > boardHeight - dino.height) {
            gameOver = true;
        }
    }

    boolean collision(Dino d, Obstacle o) {
        return d.x < o.x + o.width &&
               d.x + d.width > o.x &&
               d.y < o.y + o.height &&
               d.y + d.height > o.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placeObstacleTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -10;

            if (gameOver) {
                // Restart game
                dino.y = dinoY;
                velocityY = 0;
                obstacles.clear();
                gameOver = false;
                score = 0;
                gameLoop.start();
                placeObstacleTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Dino Flight");
        Dinoflight gamePanel = new Dinoflight();
        frame.add(gamePanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
