package terraingen;

import sun.misc.BASE64Encoder;
import terraingen.world.World;
import terraingen.world.WorldShape;
import terraingen.world.cell.TerrainCell;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.util.Random;

public class Main {

    public static void main(String[] args) {

        //get the value from the args
        int x = 0;
        int y = 0;
        int pixel = 0;
        WorldShape w = WorldShape.LANDLOCKED;
        try {
            x = Integer.parseInt(args[0]);
            y = Integer.parseInt(args[1]);
            pixel = Integer.parseInt(args[2]);
            String type = args[3];
            switch(type){
                case "LANDLOCKED":
                    w = WorldShape.LANDLOCKED;
                    break;
                case "ISLAND":
                    w = WorldShape.ISLAND;
                    break;
                case "ISLANDPLUS":
                    w = WorldShape.ISLANDPLUS;
                    break;
                case "NATION":
                    w = WorldShape.NATION;
                    break;
                case "ARCHIPELAGO":
                    w = WorldShape.ARCHIPELAGO;
            }
        }catch(Exception e){
            System.err.println("Incorrect arguments");
            System.exit(1);
        }

        String seed = "";
        try {
            //checking for seed
            seed = args[4];
        } catch (Exception e){
            seed = new Random().nextInt(Integer.MAX_VALUE) + "";
        }

	    World world = new World(x, y, seed);
        world.generateTerrain(w);
        world.generateLakes();
        world.generateRivers();
        world.generateBeaches();

        /*
            Ideas for TODO list:
                Rivers(!!!) - DONE!
                Cities/Towns/Villages (!)
                Roads between towns
                Ruins (?) - could be done in town generation
                Mountain(s) (???) - maybe lake generation minus the smoothing
                Maybe actually finish MonopolAI XD

                Think about not doing features for Archipelago coz it's going to be too hard
         */


        //printWorld(world);

        //System.out.println("");
        //System.out.println(world.toJson());
        try {
            BufferedImage bi = world.toImage(pixel, pixel);
            //ImageIO.write(bi, "PNG", new File("terrain.png"));

            //Data uri
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bi, "PNG", out);
            byte[] bytes = out.toByteArray();
            String base64bytes = Base64.getEncoder().encodeToString(bytes);
            String fin = "data:image/png;base64," + base64bytes;
            System.out.println(fin);
        } catch (Exception e){
            System.err.println("Something went wrong during image export!");
        }
    }

    public static void printWorld(World w){
        StringBuilder s;
        for (int x = 0; x < w.getXSize(); x++){
            s = new StringBuilder();
            for (int y = 0; y < w.getYSize(); y++){
                switch (((TerrainCell)w.cellAt(x, y)).getTerrainType()){
                    case PLAIN:
                        s.append("p");
                        break;
                    case FOREST:
                        s.append("f");
                        break;
                    case DESERT:
                        s.append("d");
                        break;
                    case BEACH:
                        s.append("b");
                        break;
                    case RIVER:
                        s.append("r");
                        break;
                    case LAKE:
                        s.append("l");
                        break;
                    case OCEAN:
                        s.append("o");
                        break;
                    case SAVANNAH:
                        s.append("s");
                        break;
                    case JUNGLE:
                        s.append("j");
                        break;
                }
            }
            System.out.println(s.toString());
        }
    }

}
