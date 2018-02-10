package fr.iutlens.mmi.jumper;

import android.graphics.Canvas;

import fr.iutlens.mmi.jumper.utils.SpriteSheet;

/**
 * Created by dubois on 30/12/2017.
 */

public class Level {
    public static final int VISIBLE_TILES = 10;


    // Codes pour le décor
    //   (0)   rien
    // ( = )
    // o / \
    // < ^ >   comme les deux lignes au dessus, + barrière
    // * é è
    // + (13)  // élévation du niveau de base
    // - (14)  // baisse du niveau de base

    private final String CODE = " (=)o/\\<^>*éè+-";
    private final String SLOPE ="     +-    +-";
    private String def = "(==\\^///==^==^\\\\^==/)----<^é>++(//=^=> ++o-- *- o +(==^=)";


    private int[] sprite_id;
    private int[] baseline;
    private int[] slope;

    private SpriteSheet sprite;

    public Level(int sprite_id, String s){
        if (s == null) s = def;
        parse(s);

        sprite =  SpriteSheet.get(sprite_id);
    }


    private void parse(String s){
        int size = 0;
        // Calcul de la longueur réelle du parcours (+- changent le niveau, pas la longueur)
        for(int i = 0; i <s.length(); ++i) if (s.charAt(i) != '+' && s.charAt(i) != '-') ++size;

        sprite_id = new int[size];
        baseline = new int[size];
        slope = new int[size];

        int pos = 0;
        int current_baseline = 0;
        for(int i = 0; i <s.length(); ++i){
            int code = CODE.indexOf(s.charAt(i));
            if (code<13){
                sprite_id[pos] = code-1; // -1 correspond à vide
                char sl  = SLOPE.charAt(code);
                switch (sl){
                    case ' ' :
                        slope[pos] = 0;
                        baseline[pos] = current_baseline;
                        break;
                    case '+' :
                        slope[pos] = +1;
                        ++current_baseline;
                        baseline[pos] = current_baseline;
                        break;
                    case '-' :
                        slope[pos] = -1;
                        baseline[pos] = current_baseline;
                        --current_baseline;
                        break;
                }
                ++pos;
            } else {
                if (code==13) ++current_baseline;
                else if (code == 14) --current_baseline;
            }
        }
    }


    public float getY(float y){
        return (6-y)* (sprite.h/3);
    }

    public float getX(float x){
        return x*sprite.w;
    }

    public float getWidth(){
        return VISIBLE_TILES*sprite.w;
    }

    public float getFloor(float pos){
        if (pos>=getLength()) pos = getLength()-1;
        int start = (int) Math.floor(pos);
        float offset = - (pos-start);
        float result = baseline[start];
        int s = slope[start];

        // prise en compte de l'effet de la pente(s)
        // sur la hauteur du sol en fonction de
        // la position sur la tuile (offset=o)
        //  s     o=0    o=1     formule
        // -------------------------------
        // +1  :* -1  -> 0       = -1+o
        // -1  :   0  -> -1      = -o
        //  0  :   0  -> 0       = 0
        //
        // * : les tuiles sont affichées à partir du point de le plus
        //     haut. Donc, pour une tuile montante va de 0 -> 1, mais le
        //     la référence (baseline) est celle d'arrivée. Donc un
        //     Décalage de 0 au début correspondrait à la hauteur d'arrivée,
        //     c'est à dire un cran trop haut. On corrige donc 0-1 -> 1-1,
        //     ce qui explique le -1 -> 0 (la hauteur à la fin de la tuile sera la
        //     baseline

        // Traduction des calculs précédents :
        if (s == +1) result -= 1+offset;
        if (s == -1) result += offset;
        //        if (s ==  0) result +=  0;   // inutile, += 0 ne fait rien
        return result;
    }


    public void paint(Canvas canvas, float pos){
        if (pos>=getLength()) pos = getLength()-1;
        int start = (int) Math.floor(pos);
        float offset = - (pos-start);

        for(int i = 0; i <= VISIBLE_TILES; ++i) {
                int ndx = (i+start) % sprite_id.length;
                int id =  sprite_id[ndx];
                if (id != -1) sprite.paint(canvas,id,
                        getX(offset+i),
                        getY(baseline[ndx]+1));
        }
    }

    public int getLength() {
        return sprite_id.length;
    }

    public float getSlope(float pos) {
        if (pos>=getLength()) pos = getLength()-1;
        int start = (int) Math.floor(pos);
        return slope[start];
    }
}
