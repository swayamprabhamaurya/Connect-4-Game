package com.sam_game_pac;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class game_Controller implements Initializable {
    private static final int col=7;
    private static final int row=6;
    private static final int dim=65;
    private static final String discColor1="#24303E";
    private static final String discColor2="#4CAA88" ;

    private static String play1="Player One";
    private static String play2="Player Two";
    private boolean isPlayerOneTurn=true;
    private Disc[][] discArr=new Disc[row][col];//structural change for developers
    @FXML
    public GridPane rootGridPane;
    @FXML
    public Pane insertDiscPane;
    @FXML
    public Label playerNameLabel;
    private boolean isallowedToInsert=true;
    public void createPlayground(){
       Shape rectHoles=createGSGrid();
       rootGridPane.add(rectHoles,0,1);
       List<Rectangle> rectangleList=clickableCol();
       for (Rectangle rectangle:rectangleList){
           rootGridPane.add(rectangle,0,1);
       }
    }
    private Shape createGSGrid(){
        Shape rectHoles= new Rectangle((col+1)*dim,(row+1)*dim);
        for (int r=0;r<row;r++){
            for(int c=0;c<col;c++){
                Circle circle=new Circle();
                circle.setRadius(dim/2);
                circle.setCenterX(dim/2);
                circle.setCenterY(dim/2);
                circle.setTranslateX(c*(dim+5)+dim/4);
                circle.setTranslateY(r*(dim+5)+dim/4);
                rectHoles=Shape.subtract(rectHoles,circle);
            }
        }
        rectHoles.setFill(Color.SNOW);
        return rectHoles;
    }
    private List<Rectangle> clickableCol(){
        List<Rectangle> rectangleList=new ArrayList<>();
        for (int c=0;c<col;c++){
            Rectangle rectangle=new Rectangle(dim,(row+1)*dim);
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setTranslateX(c*(dim+5)+dim/4);
            rectangle.setOnMouseEntered(mouseEvent -> rectangle.setFill(Color.valueOf("#eeeeee26")));
            rectangle.setOnMouseExited(mouseEvent -> rectangle.setFill(Color.TRANSPARENT));
            final int column=c;
            rectangle.setOnMouseClicked(mouseEvent -> {
                if (isallowedToInsert) {
                    isallowedToInsert=false;//no more disc is added
                    insertDisc(new Disc(isPlayerOneTurn),column);
                }
            });
            rectangleList.add(rectangle);
        }
        return rectangleList;
    }

    private void insertDisc(Disc disc, int column) {
        int r=row-1;
        while (r>0){
            if(discArr[r][column]==null){
                break;
            }
            r--;
        }
        if (r<0){
            return;
        }
        discArr[r][column]=disc ;//structural change for developers
        insertDiscPane.getChildren().add(disc);
        disc.setTranslateX(column*(dim+5)+dim/4);
        int currentr=r;
        TranslateTransition translateTransition=new TranslateTransition(Duration.seconds(0.3),disc);
        translateTransition.setToY(r*(dim+5)+dim/4);
        translateTransition.setOnFinished(actionEvent -> {
            isallowedToInsert=true;//when disc is droped bye player1 then next player can also do the same
            if (gameEnded(currentr,column)){
                gameOver();
                return;
            }
            isPlayerOneTurn=!isPlayerOneTurn;
            playerNameLabel.setText(isPlayerOneTurn? play1:play2);
        });
        translateTransition.play();
    }

    private void gameOver() {
        String winner=isPlayerOneTurn ? play1:play2;
        System.out.println("Winner is: "+winner);

        Alert alert=new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connect Four");
        alert.setHeaderText("The Winner is "+winner);
        alert.setContentText("Be addictive and play as long as you like (*_*)");

        ButtonType yesbtn=new ButtonType("Yes");
        ButtonType nobtn=new ButtonType("No, Exit");
        alert.getButtonTypes().setAll(yesbtn,nobtn);
        Platform.runLater(()->{
            Optional<ButtonType>btnclicked=alert.showAndWait();
            if (btnclicked.isPresent() && btnclicked.get()==yesbtn){
                resetGame();
            }else{
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public void resetGame() {
        insertDiscPane.getChildren().clear();//for all disc in pane
        for (int r=0;r<discArr.length;r++){
            for(int c=0;c<discArr[r].length;c++){
                discArr[r][c]=null;
            }
        }
        isPlayerOneTurn=true;//let player one starts the game
        playerNameLabel.setText(play1);
        createPlayground();
    }

    private boolean gameEnded(int row,int column){
        //vertical pt
        List<Point2D> vPt=IntStream.rangeClosed(row-3,row+3)//range 0 to 5
                .mapToObj(r-> new Point2D(r,column))//0,3  1,3 ...5,3
                .collect(Collectors.toList());//
        List<Point2D> hPt= IntStream.rangeClosed(column-3,column+3)
                .mapToObj(c-> new Point2D(row,c))
                .collect(Collectors.toList());
        Point2D startpt1=new Point2D(row-3,column+3);
        List<Point2D>diagonalpt1=  IntStream.rangeClosed(0,6).mapToObj(i->startpt1.add(i,-i)).collect(Collectors.toList());
        Point2D startpt2=new Point2D(row-3,column-3);
        List<Point2D>diagonalpt2=  IntStream.rangeClosed(0,6).mapToObj(i->startpt2.add(i,i)).collect(Collectors.toList());
        boolean isEnded=checkCombo(vPt)||checkCombo(hPt)||checkCombo(diagonalpt1)||checkCombo(diagonalpt2);
        return isEnded;
        }

    private boolean checkCombo(List<Point2D> points) {
        int chain=0;
        for (Point2D point:points) {
            int  rIndexArr=(int) point.getX();
            int  cIndexArr=(int) point.getY();
            Disc disc=getDiscIfPresent(rIndexArr,cIndexArr);
            if (disc != null && disc.isPlayerOnMove == isPlayerOneTurn) {
                chain++;
                if (chain == 4) {
                    return true;
                }
            }else {
                chain=0;
            }
        }
        return false;
    }
    private Disc getDiscIfPresent(int r,int c){
        if(r>=row|| r<0||c>=col||c<0){
            return null;
        }
        return discArr[r][c];
    }
    private static class Disc extends Circle{
        private final boolean isPlayerOnMove;
        public Disc(boolean isPlayerOnMove){
            this.isPlayerOnMove = isPlayerOnMove;
            setRadius(dim/2);
            setFill(isPlayerOnMove? Color.valueOf(discColor1):Color.valueOf(discColor2));
            setCenterX(dim/2);
            setCenterY(dim/2);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}