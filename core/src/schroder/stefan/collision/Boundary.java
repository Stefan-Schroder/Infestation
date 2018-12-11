package schroder.stefan.collision;

import com.badlogic.gdx.math.Vector2;

public class Boundary {
    public Vector2 startPoint;
    public float width;
    public float height;

    public Boundary(Vector2 start, float width, float height){
        this.startPoint = start;
        this.width = width;
        this.height = height;
    }

    public boolean contains(Vector2 point){
        return (startPoint.x <= point.x && point.x < startPoint.x+width && startPoint.y <= point.y && point.y < startPoint.y+height);
    }

    public boolean contains(Vector2 point, float distance){
        return (startPoint.x <= point.x + distance && point.x - distance < startPoint.x+width && startPoint.y <= point.y + distance && point.y - distance < startPoint.y+height);
    }
}