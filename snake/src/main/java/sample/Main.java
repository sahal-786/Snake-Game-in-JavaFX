package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main extends Application {
    // variable
    static int speed = 5;
    static int foodcolor = 0;
    static int width = 20;
    static int height = 20;
    static int foodX = 0;
    static int foodY = 0;
    static int cornersize = 25;
    static List<Corner> snake = new ArrayList<>();
    static List<Corner> obstacles = new ArrayList<>(); // List to store obstacles
    static Dir direction = Dir.left;
    static boolean gameOver = false;
    static Random rand = new Random();
    static Button restartButton;

    public enum Dir {
        left, right, up, down
    }

    public static class Corner {
        int x;
        int y;

        public Corner(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            newFood();
            createObstacles(); // Create obstacles

            VBox root = new VBox();
            Canvas c = new Canvas(width * cornersize, height * cornersize);
            GraphicsContext gc = c.getGraphicsContext2D();
            root.getChildren().add(c);

            // Set focus traversable to true
            c.setFocusTraversable(true);

            // Request focus for the canvas
            c.requestFocus();

            new AnimationTimer() {
                long lastTick = 0;

                public void handle(long now) {
                    if (lastTick == 0) {
                        lastTick = now;
                        tick(gc);
                        return;
                    }

                    if (now - lastTick > 1000000000 / speed) {
                        lastTick = now;
                        tick(gc);
                    }
                }
            }.start();



            Scene scene = new Scene(root, width * cornersize, height * cornersize);

            // control
            // Inside your start method
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                switch (event.getCode()) {
                    case UP:
                        if (direction != Dir.down)
                            direction = Dir.up;
                        break;
                    case DOWN:
                        if (direction != Dir.up)
                            direction = Dir.down;
                        break;
                    case LEFT:
                        if (direction != Dir.right)
                            direction = Dir.left;
                        break;
                    case RIGHT:
                        if (direction != Dir.left)
                            direction = Dir.right;
                        break;
                }
            });


            // Create restart button
            restartButton = new Button("Restart");
            restartButton.setOnAction(event -> restartGame());
            root.getChildren().add(restartButton);

            // add start snake parts
            snake.add(new Corner(width / 2, height / 2));
            snake.add(new Corner(width / 2, height / 2));
            snake.add(new Corner(width / 2, height / 2));

            primaryStage.setScene(scene);
            primaryStage.setTitle("SNAKE GAME");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void restartGame() {
        snake.clear();
        snake.add(new Corner(width / 2, height / 2));
        snake.add(new Corner(width / 2, height / 2));
        snake.add(new Corner(width / 2, height / 2));
        direction = Dir.left;
        gameOver = false;
        speed = 5;
        newFood();
    }

    // tick
    public static void tick(GraphicsContext gc) {
        if (gameOver) {
            gc.setFill(Color.RED);
            gc.setFont(new Font("", 50));
            gc.fillText("GAME OVER", 100, 250);
            restartButton.setVisible(true);
            return;
        }

        for (int i = snake.size() - 1; i >= 1; i--) {
            snake.get(i).x = snake.get(i - 1).x;
            snake.get(i).y = snake.get(i - 1).y;
        }

        switch (direction) {
            case up:
                snake.get(0).y--;
                break;
            case down:
                snake.get(0).y++;
                break;
            case left:
                snake.get(0).x--;
                break;
            case right:
                snake.get(0).x++;
                break;
        }

        // Check for collision with obstacles
        for (Corner obstacle : obstacles) {
            if (snake.get(0).x == obstacle.x && snake.get(0).y == obstacle.y) {
                gameOver = true;
                return;
            }
        }

        // Check for collision with walls
        if (snake.get(0).x < 0 || snake.get(0).x >= width || snake.get(0).y < 0 || snake.get(0).y >= height) {
            gameOver = true;
            return;
        }

        // eat food
        if (foodX == snake.get(0).x && foodY == snake.get(0).y) {
            snake.add(new Corner(-1, -1));
            newFood();
        }

        // self destroy
        for (int i = 1; i < snake.size(); i++) {
            if (snake.get(0).x == snake.get(i).x && snake.get(0).y == snake.get(i).y) {
                gameOver = true;
            }
        }

        // fill
        // background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width * cornersize, height * cornersize);

        // score
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("", 30));
        gc.fillText("Score: " + (speed - 6), 10, 30);

        // random foodcolor
        Color cc = Color.WHITE;

        switch (foodcolor) {
            case 0:
                cc = Color.PURPLE;
                break;
            case 1:
                cc = Color.LIGHTBLUE;
                break;
            case 2:
                cc = Color.YELLOW;
                break;
            case 3:
                cc = Color.PINK;
                break;
            case 4:
                cc = Color.ORANGE;
                break;
        }
        gc.setFill(cc);
        gc.fillOval(foodX * cornersize, foodY * cornersize, cornersize, cornersize);

        // snake
        for (Corner c : snake) {
            gc.setFill(Color.LIGHTGREEN);
            gc.fillRect(c.x * cornersize, c.y * cornersize, cornersize - 1, cornersize - 1);
            gc.setFill(Color.GREEN);
            gc.fillRect(c.x * cornersize, c.y * cornersize, cornersize - 2, cornersize - 2);
        }

        // Draw obstacles
        gc.setFill(Color.RED);
        for (Corner obstacle : obstacles) {
            gc.fillRect(obstacle.x * cornersize, obstacle.y * cornersize, cornersize, cornersize);
        }


    }

    // food
    public static void newFood() {
        start: while (true) {
            foodX = rand.nextInt(width);
            foodY = rand.nextInt(height);

            for (Corner c : snake) {
                if (c.x == foodX && c.y == foodY) {
                    continue start;
                }
            }
            for (Corner obstacle : obstacles) {
                if (obstacle.x == foodX && obstacle.y == foodY) {
                    continue start;
                }
            }
            foodcolor = rand.nextInt(5);
            speed++;
            break;
        }
    }

    // Create obstacles
    public static void createObstacles() {
        // Add obstacles to the list
        obstacles.add(new Corner(4, 4));
        obstacles.add(new Corner(4, 5));
        obstacles.add(new Corner(4, 6));
        obstacles.add(new Corner(4, 7));
        obstacles.add(new Corner(4, 8));
        obstacles.add(new Corner(15, 10));
        obstacles.add(new Corner(15, 9));
        obstacles.add(new Corner(15, 8));
        obstacles.add(new Corner(15, 7));
        obstacles.add(new Corner(15, 6));
        obstacles.add(new Corner(7, 15));
        obstacles.add(new Corner(8, 15));
        obstacles.add(new Corner(9, 15));
        obstacles.add(new Corner(10, 15));
        obstacles.add(new Corner(11, 15));
        obstacles.add(new Corner(12, 15));
    }

    public static void main(String[] args) {
        launch(args);
    }
}