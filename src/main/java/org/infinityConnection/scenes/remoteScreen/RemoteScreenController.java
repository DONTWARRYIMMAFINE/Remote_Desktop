package org.infinityConnection.scenes.remoteScreen;

import com.jfoenix.controls.JFXToolbar;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.infinityConnection.utils.ConnectionStatus;
import org.infinityConnection.utils.EffectType;
import org.infinityConnection.utils.EventsChangeListener;
import org.infinityConnection.utils.SceneController;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class RemoteScreenController {

    @FXML
    private Label host;

    @FXML
    private Label timer;

    @FXML
    private ImageView iw;

    @FXML
    private JFXToolbar toolbar;

    private double iwWidth;
    private double iwHeight;

    private double iwFitWidth;
    private double iwFitHeight;

    private ConnectionStatus connectionStatus = ConnectionStatus.CONNECTED;

    private Stage stage;
    private RemoteScreenModel model;
    private final ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> onResize();

    private EventsChangeListener updateConnectionStatus() {
        return new EventsChangeListener() {
            @Override
            public void onReadingChange() {
                connectionStatus = model.getConnectionStatus();
                if (connectionStatus != ConnectionStatus.CONNECTED) {
                    onDisconnect();
                    Platform.runLater(() -> SceneController.setRoot("connectScene", EffectType.EASE_OUT));
                }
            }

            @Override
            public boolean isAutoCloasable() {
                return false;
            }
        };
    }

    private EventsChangeListener updateHostName() {
        return new EventsChangeListener() {
            @Override
            public void onReadingChange() {
                Platform.runLater(() -> host.setText(model.getHostName()));
            }

            @Override
            public boolean isAutoCloasable() {
                return true;
            }
        };
    }

    private EventsChangeListener updateTimer() {
        return new EventsChangeListener() {
            @Override
            public void onReadingChange() {
                Platform.runLater(() -> timer.setText(model.getSessionTime()));
            }

            @Override
            public boolean isAutoCloasable() {
                return false;
            }
        };
    }

    private EventsChangeListener updateScreen() {
        return new EventsChangeListener() {
            @Override
            public void onReadingChange() {
                Image image = iw.getImage();
                Platform.runLater(() -> {
                    iw.setImage(model.getReceivedImage());
                    if (image == null) {
                        onResize();
                    }
                });
            }

            @Override
            public boolean isAutoCloasable() {
                return false;
            }
        };
    }

    private void onResize() {
        try {
            iwFitWidth = iw.getScene().getWidth();
            iwFitHeight = iw.getScene().getHeight() - toolbar.getHeight();

            iw.setFitWidth(iwFitWidth);
            iw.setFitHeight(iwFitHeight);

            Image image = iw.getImage();
            iwWidth = image.getWidth();
            iwHeight = image.getHeight();

            model.addListener(onMouseMoved());
        } catch (NullPointerException e) {}
    }

    //Mouse events
    private EventsChangeListener onMouseMoved() {
        return new EventsChangeListener() {
            @Override
            public void onReadingChange() {
                iw.setOnMouseClicked(model.getMouseMovedEH(iwWidth, iwHeight, iwFitWidth, iwFitHeight));
            }

            @Override
            public boolean isAutoCloasable() {
                return true;
            }
        };
    }

    private EventsChangeListener onMousePressed() {
        return new EventsChangeListener() {
            @Override
            public void onReadingChange() {
                iw.setOnMousePressed(model.getMousePressedEH());
            }

            @Override
            public boolean isAutoCloasable() {
                return true;
            }
        };
    }

    private EventsChangeListener onMouseReleased() {
        return new EventsChangeListener() {
            @Override
            public void onReadingChange() {
                iw.setOnMouseReleased(model.getMouseReleasedEH());
            }

            @Override
            public boolean isAutoCloasable() {
                return true;
            }
        };
    }

    //Keyboard events
    private EventsChangeListener onKeyPressed() {
        return new EventsChangeListener() {
            @Override
            public void onReadingChange() {
                iw.setOnKeyTyped(model.getKeyPressedEH());
            }

            @Override
            public boolean isAutoCloasable() {
                return true;
            }
        };
    }

    private EventsChangeListener onKeyReleased() {
        return new EventsChangeListener() {
            @Override
            public void onReadingChange() {
                iw.setOnKeyReleased(model.getKeyReleasedEH());
            }

            @Override
            public boolean isAutoCloasable() {
                return true;
            }
        };
    }

    private void closeWindowEvent(WindowEvent event) {
        model.shutDown();
    }

    public void exchangeData(DataInputStream dis, DataOutputStream dos) {
        iw.setImage(null);
        model = new RemoteScreenModel(dis, dos);

        stage = (Stage) SceneController.scene.getWindow();
        stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);

        stage.widthProperty().addListener(stageSizeListener);
        stage.heightProperty().addListener(stageSizeListener);

        model.addListener(updateConnectionStatus());
        model.addListener(updateHostName());
        model.addListener(updateTimer());
        model.addListener(updateScreen());


        //Events
        model.addListener(onMousePressed());
        model.addListener(onMouseReleased());

        model.addListener(onKeyPressed());
        model.addListener(onKeyReleased());

        Platform.runLater(() -> iw.requestFocus() );
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public void onDisconnect() {
        stage.widthProperty().removeListener(stageSizeListener);
        stage.heightProperty().removeListener(stageSizeListener);

        model.shutDown();
        Platform.runLater(() -> model.removeListeners());
    }
}
