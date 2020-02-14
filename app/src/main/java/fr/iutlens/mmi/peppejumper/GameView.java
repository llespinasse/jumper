package fr.iutlens.mmi.peppejumper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import fr.iutlens.mmi.peppejumper.utils.AccelerationProxy;
import fr.iutlens.mmi.peppejumper.utils.RefreshHandler;
import fr.iutlens.mmi.peppejumper.utils.SpriteSheet;
import fr.iutlens.mmi.peppejumper.utils.TimerAction;

public class GameView extends View implements TimerAction, AccelerationProxy.AccelerationListener {
    public static final float SPEED = 0.1f;
    private RefreshHandler timer;
    private Level level;
    private float current_pos;
    private Hero hero;
    private double prep;

    public GameView(Context context) {
        super(context);
        init(null, 0);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Initialisation de la vue
     *
     * Tous les constructeurs (au-dessus) renvoient ici.
     *
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle) {

        // Chargement des feuilles de sprites
        SpriteSheet.register(R.drawable.decor_running,3,4,this.getContext());
        level = new Level(R.drawable.decor_running,null);
        SpriteSheet.register(R.drawable.running_rabbit,3,3,this.getContext());
        hero = new Hero(R.drawable.running_rabbit,SPEED);



        // Gestion du rafraichissement de la vue. La méthode update (juste en dessous)
        // sera appelée toutes les 30 ms
        timer = new RefreshHandler(this);

        // Un clic sur la vue lance (ou relance) l'animation

        this.setOnTouchListener(new OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View view, MotionEvent motionEvent) {
                                        if (!timer.isRunning()) timer.scheduleRefresh(30);

                                        hero.jump(1);
                                        return true;
                                    }
                                }

        );
    }

    /**
     * Mise à jour (faite toutes les 30 ms)
     */
    @Override
    public void update() {
        if (this.isShown()) { // Si la vue est visible
            if (hero.vie <=0){
                Intent intent = new Intent(getContext(),SplashActivity.class);
                getContext().startActivity(intent);
                ((AppCompatActivity )getContext()).finish();
            } else timer.scheduleRefresh(30); // programme le prochain rafraichissement
            current_pos += SPEED;
            if (current_pos>level.getLength()) current_pos = 0;
            hero.update(level.getFloor(current_pos+1),level.getSlope(current_pos+1));
            invalidate(); // demande à rafraichir la vue
        }
    }

    /**
     * Méthode appelée (automatiquement) pour afficher la vue
     * C'est là que l'on dessine le décor et les sprites
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // On met une couleur de fond
        canvas.drawColor(0xFFFFE7AB);

        // On choisit la transformation à appliquer à la vue i.e. la position
        // de la "camera"
        setCamera(canvas);

        // Dessin des différents éléments
        level.paint(canvas,current_pos);

        float x = 1;
        float y = hero.getY();
        hero.paint(canvas,level.getX(x),level.getY(y));


    }

    private void setCamera(Canvas canvas) {

        float scale = getWidth()/level.getWidth();

        // La suite de transfomations est à interpréter "à l'envers"

        canvas.translate(0,getHeight()/2);

        // On mets à l'échelle calculée au dessus
        canvas.scale(scale, scale);

        // On centre sur la position actuelle de la voiture (qui se retrouve en 0,0 )
//        canvas.translate(0,-level.getY(hero.getY()));

    }


    @Override
    public void onAcceleration(float accelDelta, double dt) {
//        Log.d("onAcceleration", accelDelta+" "+dt);
        if (accelDelta>0.5f){
            hero.jump((float) Math.abs(accelDelta));
        }
/*        if (accelDelta<0)
            prep += -accelDelta;
            if (prep > hero.MAX_STRENGTH) {
                hero.jump((float) prep);
                prep = 0;
            }
        else {
            if (prep > 0.1) {
                hero.jump((float) prep);
            }
            prep = 0;
        }*/
    }
}
