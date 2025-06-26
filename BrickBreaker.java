import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class BrickBreaker extends JPanel implements ActionListener, KeyListener {
    private static final int BOARD_WIDTH = 800;
    private static final int BOARD_HEIGHT = 600;
    private static final int PADDLE_WIDTH = 100;
    private static final int PADDLE_HEIGHT = 10;
    private static final int BALL_SIZE = 20;
    private static final int BRICK_WIDTH = 75;
    private static final int BRICK_HEIGHT = 20;
    private static final int BRICK_ROWS = 5;
    private static final int BRICK_COLS = 10;
    
    private Timer gameTimer;
    private boolean gameStarted = false;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private int score = 0;
    private int lives = 3;
    
    // Game objects
    private Rectangle paddle;
    private Rectangle ball;
    private int ballDx = 3;
    private int ballDy = -3;
    private List<Rectangle> bricks;
    private List<Color> brickColors;
    
    // Input
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    
    public BrickBreaker() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        initializeGame();
        
        gameTimer = new Timer(16, this); // ~60 FPS
        gameTimer.start();
    }
    
    private void initializeGame() {
        // Initialize paddle
        paddle = new Rectangle(BOARD_WIDTH / 2 - PADDLE_WIDTH / 2, 
                              BOARD_HEIGHT - 50, PADDLE_WIDTH, PADDLE_HEIGHT);
        
        // Initialize ball
        ball = new Rectangle(BOARD_WIDTH / 2 - BALL_SIZE / 2, 
                            BOARD_HEIGHT / 2, BALL_SIZE, BALL_SIZE);
        
        // Initialize bricks
        bricks = new ArrayList<>();
        brickColors = new ArrayList<>();
        for (int row = 0; row < BRICK_ROWS; row++) {
            for (int col = 0; col < BRICK_COLS; col++) {
                int x = col * (BRICK_WIDTH + 5) + 37;
                int y = row * (BRICK_HEIGHT + 5) + 50;
                bricks.add(new Rectangle(x, y, BRICK_WIDTH, BRICK_HEIGHT));
                
                // Different colors for different rows
                Color color;
                switch (row) {
                    case 0: color = Color.RED; break;
                    case 1: color = Color.ORANGE; break;
                    case 2: color = Color.YELLOW; break;
                    case 3: color = Color.GREEN; break;
                    case 4: color = Color.BLUE; break;
                    default: color = Color.WHITE;
                }
                brickColors.add(color);
            }
        }
    }
    
    private void resetBall() {
        ball.x = BOARD_WIDTH / 2 - BALL_SIZE / 2;
        ball.y = BOARD_HEIGHT / 2;
        ballDx = 3;
        ballDy = -3;
        gameStarted = false;
    }
    
    private void update() {
        if (gameOver || gameWon) return;
        
        // Move paddle
        if (leftPressed && paddle.x > 0) {
            paddle.x -= 8;
        }
        if (rightPressed && paddle.x < BOARD_WIDTH - PADDLE_WIDTH) {
            paddle.x += 8;
        }
        
        if (!gameStarted) return;
        
        // Move ball
        ball.x += ballDx;
        ball.y += ballDy;
        
        // Ball collision with walls
        if (ball.x <= 0 || ball.x >= BOARD_WIDTH - BALL_SIZE) {
            ballDx = -ballDx;
        }
        if (ball.y <= 0) {
            ballDy = -ballDy;
        }
        
        // Ball falls below paddle
        if (ball.y >= BOARD_HEIGHT) {
            lives--;
            if (lives <= 0) {
                gameOver = true;
            } else {
                resetBall();
            }
        }
        
        // Ball collision with paddle
        if (ball.intersects(paddle)) {
            ballDy = -ballDy;
            
            // Add some angle based on where ball hits paddle
            int paddleCenter = paddle.x + PADDLE_WIDTH / 2;
            int ballCenter = ball.x + BALL_SIZE / 2;
            int difference = ballCenter - paddleCenter;
            ballDx = difference / 10;
            
            // Ensure minimum speed
            if (Math.abs(ballDx) < 2) {
                ballDx = ballDx >= 0 ? 2 : -2;
            }
        }
        
        // Ball collision with bricks
        for (int i = bricks.size() - 1; i >= 0; i--) {
            Rectangle brick = bricks.get(i);
            if (ball.intersects(brick)) {
                bricks.remove(i);
                brickColors.remove(i);
                ballDy = -ballDy;
                score += 10;
                
                // Check win condition
                if (bricks.isEmpty()) {
                    gameWon = true;
                }
                break;
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
    
    private void draw(Graphics g) {
        // Draw paddle
        g.setColor(Color.WHITE);
        g.fillRect(paddle.x, paddle.y, paddle.width, paddle.height);
        
        // Draw ball
        g.setColor(Color.WHITE);
        g.fillOval(ball.x, ball.y, ball.width, ball.height);
        
        // Draw bricks
        for (int i = 0; i < bricks.size(); i++) {
            Rectangle brick = bricks.get(i);
            g.setColor(brickColors.get(i));
            g.fillRect(brick.x, brick.y, brick.width, brick.height);
            g.setColor(Color.BLACK);
            g.drawRect(brick.x, brick.y, brick.width, brick.height);
        }
        
        // Draw UI
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Score: " + score, 10, 25);
        g.drawString("Lives: " + lives, 10, 50);
        
        // Draw game states
        if (!gameStarted && !gameOver && !gameWon) {
            g.setFont(new Font("Arial", Font.BOLD, 24));
            String msg = "Press SPACE to Start";
            FontMetrics fm = g.getFontMetrics();
            int x = (BOARD_WIDTH - fm.stringWidth(msg)) / 2;
            g.drawString(msg, x, BOARD_HEIGHT / 2 + 50);
        }
        
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String msg = "GAME OVER";
            FontMetrics fm = g.getFontMetrics();
            int x = (BOARD_WIDTH - fm.stringWidth(msg)) / 2;
            g.drawString(msg, x, BOARD_HEIGHT / 2);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            msg = "Press R to Restart";
            fm = g.getFontMetrics();
            x = (BOARD_WIDTH - fm.stringWidth(msg)) / 2;
            g.drawString(msg, x, BOARD_HEIGHT / 2 + 50);
        }
        
        if (gameWon) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String msg = "YOU WIN!";
            FontMetrics fm = g.getFontMetrics();
            int x = (BOARD_WIDTH - fm.stringWidth(msg)) / 2;
            g.drawString(msg, x, BOARD_HEIGHT / 2);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            msg = "Press R to Restart";
            fm = g.getFontMetrics();
            x = (BOARD_WIDTH - fm.stringWidth(msg)) / 2;
            g.drawString(msg, x, BOARD_HEIGHT / 2 + 50);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            leftPressed = true;
        }
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            rightPressed = true;
        }
        if (key == KeyEvent.VK_SPACE && !gameStarted && !gameOver && !gameWon) {
            gameStarted = true;
        }
        if (key == KeyEvent.VK_R && (gameOver || gameWon)) {
            restartGame();
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            leftPressed = false;
        }
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            rightPressed = false;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    private void restartGame() {
        gameOver = false;
        gameWon = false;
        gameStarted = false;
        score = 0;
        lives = 3;
        initializeGame();
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Brick Breaker");
        BrickBreaker game = new BrickBreaker();
        
        frame.add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}