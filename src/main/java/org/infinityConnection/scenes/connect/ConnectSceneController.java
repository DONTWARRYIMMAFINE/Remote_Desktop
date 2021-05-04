package org.infinityConnection.scenes.connect;

import javafx.animation.RotateTransition;
import javafx.animation.StrokeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.infinityConnection.utils.ConnectionStatus;
import org.infinityConnection.utils.EffectType;
import org.infinityConnection.utils.SceneController;

import java.util.ArrayList;
import java.util.List;

public class ConnectSceneController {

    @FXML
    private Label status;

    @FXML
    private Circle circle1;

    @FXML
    private Circle circle2;

    @FXML
    private Circle circle3;

    private ConnectSceneModel model;
    private ConnectionStatus connectionStatus = ConnectionStatus.UNKNOWN;
    private final List<Circle> circles = new ArrayList<>();
    private final List<Paint> normalColors = new ArrayList<>();

    private void setRotation(Circle c, boolean reverse, int angle, int duration) {
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(duration), c);

        rotateTransition.setAutoReverse(reverse);

        rotateTransition.setByAngle(angle);
        rotateTransition.setDelay(Duration.seconds(0));
        rotateTransition.setRate(3);
        rotateTransition.setCycleCount(18);

        rotateTransition.setOnFinished((e) -> setRotation(c, reverse, angle, duration));
        rotateTransition.play();
    }

    private void setSuccessColors() {
        for (Circle circle : circles) {
            StrokeTransition strokeTransition = new StrokeTransition(Duration.seconds(1), circle);
            strokeTransition.setToValue(Color.GREEN);
            strokeTransition.play();
        }
    }

    private void setErrorColors() {
        for (Circle circle : circles) {
            StrokeTransition strokeTransition = new StrokeTransition(Duration.seconds(1), circle);
            strokeTransition.setToValue(Color.RED);
            strokeTransition.play();
        }
    }

    private void setNormalColors() {
        for (int i = 0; i < circles.size(); i++) {
            circles.get(i).setStroke(normalColors.get(i));
        }
    }

    private void updateConnectionStatus() {
        System.out.println(connectionStatus.getStatusName());

        connectionStatus = model.getConnectionStatus();

        if (connectionStatus != ConnectionStatus.CONNECTING) {
            updateLabel();
            if (connectionStatus != ConnectionStatus.CONNECTED) {
                setErrorColors();
                changeScene();
            } else {
                setSuccessColors();
                model.shutDown();
            }
        }

    }

    private void updateLabel() {
        Platform.runLater(() -> status.setText(connectionStatus.getStatusName()));
    }

    private void changeScene() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> SceneController.setRoot("mainScene", EffectType.EASE_OUT));
    }

    private void initColorsArray() {
        for (Circle circle : circles) {
            normalColors.add(circle.getStroke());
        }
    }

    public void initialize() {
        circles.add(circle1);
        circles.add(circle2);
        circles.add(circle3);

        initColorsArray();

        setRotation(circle1, true, 360, 10);
        setRotation(circle2, true, 180, 18);
        setRotation(circle3, true, 145, 24);
    }

    public void establishConnection(String ip, int port, String password) {
        setNormalColors();
        status.setText(ConnectionStatus.CONNECTING.getStatusName());

        model = new ConnectSceneModel(ip, port, password);
        model.addListener(this::updateConnectionStatus);
    }

}