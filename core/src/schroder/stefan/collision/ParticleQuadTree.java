package schroder.stefan.collision;

import java.util.LinkedList;

import com.badlogic.gdx.math.Vector2;

import schroder.stefan.particle.ZombieParticle;

public class ParticleQuadTree {

    private Boundary boundary;
    private int capacity;
    private LinkedList<ZombieParticle> points;
    private boolean divided;

    private ParticleQuadTree NE;
    private ParticleQuadTree NW;
    private ParticleQuadTree SE;
    private ParticleQuadTree SW;

    public ParticleQuadTree(Boundary rectangle, int capacity){
        this.boundary = rectangle;
        this.capacity = capacity;
        points = new LinkedList<ZombieParticle>();
        divided = false;
    }

    public boolean insert(ZombieParticle point){
        if(!boundary.contains(point.getPosition())){
            return false;
        }
        if(points.size() < capacity){
            points.add(point);
            return true;
        }else{
            if(!divided) subdivide();

            if(NE.insert(point)) return true;
            else if(NW.insert(point)) return true;
            else if(SE.insert(point)) return true;
            else if(SW.insert(point)) return true;
            return false;
        }
    }

    public void query(Vector2 point, float distance, LinkedList<ZombieParticle> parts){
        //LinkedList<particle> parts = new LinkedList<particle>();
        if(boundary.contains(point,distance)){
            for(ZombieParticle eachpart : points){
                if(eachpart.getPosition().dst(point)<=distance){
                    parts.add(eachpart);
                }
            }

            if(divided){

                NE.query(point, distance, parts);
                NW.query(point, distance, parts);
                SE.query(point, distance, parts);
                SW.query(point, distance, parts);

            }
        }

        //return parts;
    }

    public void subdivide(){
        NE = new ParticleQuadTree(  new Boundary(   new Vector2(boundary.startPoint.x + boundary.width/2,
                                                        boundary.startPoint.y),
                                            boundary.width/2, 
                                            boundary.height/2), 
                            capacity
                        );
        NW = new ParticleQuadTree(  new Boundary(   boundary.startPoint, 
                                            boundary.width/2, 
                                            boundary.height/2
                                        ), 
                            capacity
                        );
        SE = new ParticleQuadTree(  new Boundary(   new Vector2(boundary.startPoint.x + boundary.width/2,
                                                        boundary.startPoint.y + boundary.height/2
                                                       ),
                                            boundary.width/2, 
                                            boundary.height/2
                                        ), 
                            capacity
                        );
        SW = new ParticleQuadTree(  new Boundary(   new Vector2(boundary.startPoint.x,
                                                        boundary.startPoint.y + boundary.height/2
                                                       ),
                                            boundary.width/2, 
                                            boundary.height/2
                                        ), 
                            capacity
                        );

        divided = true;
    }
    
}