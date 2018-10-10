package schroder.stefan.particle;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

//Other classes
import schroder.stefan.collision.ParticleQuadTree;
import schroder.stefan.collision.Boundary;
import schroder.stefan.WeaponDamage;
import schroder.stefan.Map;
import schroder.stefan.objects.Vehicle;

public class ParticleController extends Thread{
    private final int width;
    private final int height;
    private boolean running;

    public LinkedList<ZombieParticle> liveParticle;
    public LinkedList<ZombieParticle> deadParticle;
    private ConcurrentLinkedQueue<ZombieParticle> unbornParticles;
    private ParticleQuadTree liveParticleQuad;


    private long currentTime;
    private float deltaTime;

    public long runCount;

    public ConcurrentLinkedQueue<WeaponDamage> damage;

    private Random random;

    private Map map;

    public Vehicle currentCar;

    public ParticleController(int width, int height, Map map){
        this.width = width;
        this.height = height;
        this.map = map;
        deadParticle = new LinkedList<ZombieParticle>();
        liveParticle = new LinkedList<ZombieParticle>();
        unbornParticles = new ConcurrentLinkedQueue<ZombieParticle>();

        damage = new ConcurrentLinkedQueue<WeaponDamage>();

        running = true;
        random = new Random();

        this.currentCar = null;

    }

    public void run(){
        while(running){

            //#region conditional wait
            while(System.nanoTime()-currentTime<=10){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //#endregion

            deltaTime = DeltaTime();
            currentTime = System.nanoTime();

            //#region updateCar
            if(currentCar!=null)
                currentCar.update(deltaTime);
            //#endregion

            //#region kill dead particles
            Iterator liveIter = liveParticle.iterator();
            while(liveIter.hasNext()){

                ZombieParticle onePart = (ZombieParticle) liveIter.next();

                if(onePart.isDead()){

                    deadParticle.add(onePart);
                    liveIter.remove();

                }
            }
            //#endregion

            //#region process damage
            int initailSize = damage.size();

            for(int i=0; i<initailSize; i++){

                WeaponDamage currentDamage = damage.poll();

                if(!currentDamage.isTime(TimeUtils.nanoTime())){

                    damage.add(currentDamage);

                }else{

                    LinkedList<ZombieParticle> affectedParticles = new LinkedList<ZombieParticle>();
                    liveParticleQuad.query(currentDamage.position, currentDamage.size, affectedParticles);

                    for(ZombieParticle eachParticle : affectedParticles){

                        if(eachParticle.getPosition().dst(currentDamage.position) < currentDamage.killZone) eachParticle.kill();

                        else{

                            //commits the affect 100x will need to be fixed later
                            for(int j=0; j<100; j++){

                                eachParticle.seek(currentDamage.position, -currentDamage.force);

                            }
                        }
                    }

                    //make crater
                    if(currentDamage.gun!=0) map.addHole(currentDamage.position, currentDamage.size);
                }
            }
            //#endregion

            //#region reinsert all living particles into Qtree
            liveParticleQuad = new ParticleQuadTree(new Boundary(new Vector2(0,0), width, height), 4);
            for(ZombieParticle parts : liveParticle){
                liveParticleQuad.insert(parts);
            }
            //#endregion

            //#region update particles
            liveParticle.parallelStream().forEach(onePart -> {

                LinkedList<ZombieParticle> closeParts = new LinkedList<ZombieParticle>();
                liveParticleQuad.query(onePart.getPosition(), 10, closeParts);
                //onePart.seek(new Vector2(width/2,height/2),0.5f );
                if(currentCar!=null)
                    onePart.seek(currentCar.getPosition(), 0.5f);
                onePart.separate(closeParts, 5);
                onePart.align(closeParts, 1);
                onePart.cohesion(closeParts, 0.5f);
                //onePart.flock(liveParticle, 3,1,0.5f);
                onePart.follow(2);
                onePart.update(deltaTime);

            });
            //#endregion

            birthParticles();

        }
    }

    private float DeltaTime(){
        return (float) (Float.valueOf(System.nanoTime()-currentTime)*Math.pow(10,-9));
    }

    public void createParticle(Vector2 position){
        if (deadParticle.size() > 10) {
            ZombieParticle revivePart = deadParticle.pop();
            revivePart.reset(position);
            unbornParticles.add(revivePart);
        } else {
            ZombieParticle newPart = new ZombieParticle(position, width, height, map);
            unbornParticles.add(newPart);
        }
    }

    private void birthParticles(){
        int birth = unbornParticles.size();
        for(int i=0; i<birth; i++) {
            liveParticle.add(unbornParticles.poll());

        }
    }

    public String getSizes(){
        return "Live: "+String.valueOf(liveParticle.size())+" | Dead: "+String.valueOf(deadParticle.size())+" | unborn: "+String.valueOf(unbornParticles.size());
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public LinkedList<ZombieParticle> getLiveParticle() {
        return (LinkedList<ZombieParticle>) liveParticle.clone();
    }

    public LinkedList<ZombieParticle> getDeadParticle() {
        return (LinkedList<ZombieParticle>) deadParticle.clone();
    }

}
