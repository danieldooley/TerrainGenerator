package terraingen.world;

/**
 * Created by dandooley on 18/02/16.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import terraingen.world.cell.Cell;
import terraingen.world.cell.Terrain;
import terraingen.world.cell.TerrainCell;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class represents the entirety of a generated
 * world. A world is split into cells which are stored
 * individually in a 2d matrix.
 */


public class World implements Serializable {

    private final Integer x;
    private final Integer y;

    private final String seed;

    private List<List<Cell>> map;

    private Random r;

    private WorldShape shape;

    //Flags
    private boolean terrain = false;

    /**
     * Sets up a world
     *
     * @param x Number of chunks across
     * @param y Number of chunks up
     */
    public World(Integer x, Integer y, String seed) {
        this.x = x;
        this.y = y;

        this.map = new ArrayList<>(x);

        for (int i = 0; i < x; i++) {
            ArrayList<Cell> c = new ArrayList<>(y);
            map.add(c);
        }

        this.seed = seed;

        r = new Random(seed.hashCode());
    }

    /**
     * This function creates the basic terrain of the world
     */
    public void generateTerrain(WorldShape w) {

        this.shape = w;

        //generate big shapes of a specific number
        Integer[][] squareArray = new Integer[x][y];
        Integer times = 100;
        for (int i = 0; i < times; i++) {
            Integer randX = r.nextInt(x);
            Integer randY = r.nextInt(y);
            Integer xSize = r.nextInt(x / 4);
            Integer ySize = r.nextInt(y / 4);
            Integer num = r.nextInt(200);
            for (int a = -(xSize / 2); a < xSize / 2; a++) {
                for (int b = -(ySize / 2); b < ySize / 2; b++) {
                    if (randX + a > 0 && randY + b > 0) {
                        if (randX + a < x && randY + b < y) {
                            if (!(Math.abs(a + b) > xSize || Math.abs(a + b) > ySize)) {
                                squareArray[randX + a][randY + b] = num;
                            }
                        }
                    }
                }
            }
        }

        //fill any blanks
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (squareArray[i][j] == null) {
                    squareArray[i][j] = r.nextInt(200);
                }
            }
        }

        //add a bunch of random numbers
        Integer[][] randArray = new Integer[x][y];
        times = 40;
        for (int z = 0; z < times; z++) {
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    randArray[i][j] = squareArray[i][j] + ((r.nextInt(3) - 1) * r.nextInt(200));
                    if (z == times - 1) {
                    }
                }
                if (z == times - 1) {
                }
            }
        }

        //average these numbers
        Integer rep = 2;
        Integer[][] averageArray = new Integer[1][1];
        for (int s = 0; s < rep; s++) {
            averageArray = new Integer[x][y];
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {

                    Integer sum = 0;
                    Integer count = 0;
                    for (int a = -1; a < 2; a++) {
                        for (int b = -1; b < 2; b++) {
                            if (i + a > 0 && j + b > 0) {
                                if (i + a < x && j + b < y) {
                                    count++;
                                    sum += randArray[i + a][j + b];
                                }
                            }
                        }
                    }
                    Integer average = sum / count;
                    averageArray[i][j] = average;
                    if (s == rep - 1) {
                    }
                }
                if (s == rep - 1) {
                }
            }
            randArray = averageArray;
        }

        //find the min and max
        Integer min = 100;
        Integer max = 0;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                Integer val = averageArray[i][j];
                if (val < min) {
                    min = val;
                }
                if (val > max) {
                    max = val;
                }
            }
        }

        Integer[][] finalArray = new Integer[x][y];
        // use max and min to map values to 1-5
        Integer range = max - min;
        Double step = (range + 0.0) / 6;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                Integer val = averageArray[i][j] - min;
                Double dval = val / step;
                Integer fin = (int) Math.round(dval);
                finalArray[i][j] = fin;
            }
        }

        //then we use the noise to decide the terrain type

        Long id = 1L;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                Terrain[] types = {Terrain.DESERT, Terrain.DESERT, Terrain.SAVANNAH, Terrain.PLAIN, Terrain.FOREST, Terrain.JUNGLE, Terrain.JUNGLE};
                map.get(i).add(new TerrainCell(id, types[finalArray[i][j]]));
                id++;
            }
        }

        //this give us our base terrain.
        //next is to create oceans based on the selected map type

        switch (w) {
            case NATION:
                //a nation has water on one random side
                Integer side = r.nextInt(4);
                if (side == 1 || side == 3) {
                    //left or right
                    int move = (r.nextInt(x / 10) + 3);
                    for (int j = 0; j < y; j++) {

                        if (side == 1) {
                            for (int i = x - 1; i >= x - 1 - move; i--) {
                                Cell current = map.get(i).get(j);
                                map.get(i).set(j, new TerrainCell(current.getId(), Terrain.OCEAN));
                            }
                        } else {
                            for (int i = 0; i <= move; i++) {
                                Cell current = map.get(i).get(j);
                                map.get(i).set(j, new TerrainCell(current.getId(), Terrain.OCEAN));
                            }
                        }

                        int change = r.nextInt(3) - 1;
                        if (move + change == x / 10) {
                            change = -1;
                        } else if (move + change == 3) {
                            change = 1;
                        }
                        move += change;
                    }
                } else {
                    //top or bottom
                    int move = (r.nextInt(y / 10) + 3);
                    for (int i = 0; i < x; i++) {

                        if (side == 0) {
                            for (int j = y - 1; j >= y - 1 - move; j--) {
                                Cell current = map.get(i).get(j);
                                map.get(i).set(j, new TerrainCell(current.getId(), Terrain.OCEAN));
                            }
                        } else {
                            for (int j = 0; j <= move; j++) {
                                Cell current = map.get(i).get(j);
                                map.get(i).set(j, new TerrainCell(current.getId(), Terrain.OCEAN));
                            }
                        }

                        int change = r.nextInt(3) - 1;
                        if (move + change == y / 10) {
                            change = -1;
                        } else if (move + change == 3) {
                            change = 1;
                        }
                        move += change;
                    }
                }
                break;
            case ISLAND:
                //water on all sides
                int move = (r.nextInt(x / 5) + 3);
                for (int j = 0; j < y; j++) {

                    for (int i = x - 1; i >= x - 1 - move; i--) {
                        Cell current = map.get(i).get(j);
                        map.get(i).set(j, new TerrainCell(current.getId(), Terrain.OCEAN));
                    }

                    int change = r.nextInt(3) - 1;
                    if (move + change == x / 5) {
                        change = -1;
                    } else if (move + change == 3) {
                        change = 1;
                    }
                    move += change;

                }
                for (int j = 0; j < y; j++) {

                    for (int i = 0; i <= move; i++) {
                        Cell current = map.get(i).get(j);
                        map.get(i).set(j, new TerrainCell(current.getId(), Terrain.OCEAN));
                    }

                    int change = r.nextInt(3) - 1;
                    if (move + change == x / 5) {
                        change = -1;
                    } else if (move + change == 3) {
                        change = 1;
                    }
                    move += change;

                }
                for (int i = 0; i < x; i++) {

                    for (int j = y - 1; j >= y - 1 - move; j--) {
                        Cell current = map.get(i).get(j);
                        map.get(i).set(j, new TerrainCell(current.getId(), Terrain.OCEAN));
                    }

                    int change = r.nextInt(3) - 1;
                    if (move + change == y / 5) {
                        change = -1;
                    } else if (move + change == 3) {
                        change = 1;
                    }
                    move += change;
                }

                for (int i = 0; i < x; i++) {

                    for (int j = 0; j <= move; j++) {
                        Cell current = map.get(i).get(j);
                        map.get(i).set(j, new TerrainCell(current.getId(), Terrain.OCEAN));
                    }

                    int change = r.nextInt(3) - 1;
                    if (move + change == y / 5) {
                        change = -1;
                    } else if (move + change == 3) {
                        change = 1;
                    }
                    move += change;
                }
                break;
            case ARCHIPELAGO:
            case ISLANDPLUS:
                //Do Archipelago like lake generation but larger and inverse
                //Just gonna shamelessly reuse some lake code, don't mind me
                // (read 'lake' below as a metaphor of an island in the ocean)

                //in doing this I found that by making enough overlapping 'lakes' it generates a way more interesting island
                // hence 'ISLANDPLUS'

                //build a large array of zeros, we'll stamp the 'lakes' into this rather than straight onto the map
                int[][] archArray = new int[x][y];
                for (int i = 0; i < x; i++) {
                    for (int j = 0; j < y; j++) {
                        archArray[i][j] = 0;
                    }
                }

                Integer num;
                if (w == WorldShape.ISLANDPLUS){
                    num = r.nextInt(Math.round((x * y) / 150));
                } else {
                    num = r.nextInt(Math.round((x * y) / 650));
                }

                //make a random number of lakes
                for (int z = 0; z < num; z++) {
                    Integer xSize = r.nextInt(x / 3) + 4;
                    Integer ySize = r.nextInt(y / 3) + 4;

                    //set the square to all water
                    int[][] lake = new int[xSize][ySize];
                    for (int i = 0; i < xSize; i++) {
                        for (int j = 0; j < ySize; j++) {
                            lake[i][j] = 1;
                        }
                    }

                    //randomly shave in from each side
                    for (int j = 0; j < ySize; j++) {
                        //for each row
                        int in = r.nextInt(xSize / 4) + 1;
                        for (int q = 0; q < in; q++) {
                            lake[q][j] = 0;
                        }
                        in = r.nextInt(xSize / 4) + 1;
                        for (int q = 0; q < in; q++) {
                            lake[(xSize - 1) - q][j] = 0;
                        }
                    }
                    for (int i = 0; i < xSize; i++) {
                        //for each column
                        int in = r.nextInt(ySize / 4) + 1;
                        for (int q = 0; q < in; q++) {
                            lake[i][q] = 0;
                        }
                        in = r.nextInt(ySize / 4) + 1;
                        for (int q = 0; q < in; q++) {
                            lake[i][(ySize - 1) - q] = 0;
                        }
                    }

                    //use a 'Game of Life' style rule to smooth edges
                    //ground with 3 neighbouring water becomes water
                    //water with less that 2 neighbouring ground becomes ground
                    //we need to do this multiple times to deal with 'spiky' looking lakes on larger maps

                    int s = Math.max(xSize, ySize) / 10;
                    int[][] finalLake = new int[xSize][ySize];
                    for (int q = 0; q < s; q++) {
                        finalLake = new int[xSize][ySize];
                        for (int i = 1; i < xSize - 1; i++) {
                            for (int j = 1; j < ySize - 1; j++) {
                                Integer count = 0;
                                //count neighbouring water (up, down, left, right)
                                if (lake[i + 1][j] == 1) {
                                    count++;
                                }
                                if (lake[i][j + 1] == 1) {
                                    count++;
                                }
                                if (lake[i - 1][j] == 1) {
                                    count++;
                                }
                                if (lake[i][j - 1] == 1) {
                                    count++;
                                }
                                if (lake[i][j] == 1) {
                                    if (count < 3) {
                                        finalLake[i][j] = 0;
                                    } else {
                                        finalLake[i][j] = 1;
                                    }
                                } else {
                                    if (count > 2) {
                                        finalLake[i][j] = 1;
                                    } else {
                                        finalLake[i][j] = 0;
                                    }
                                }
                            }
                        }
                        lake = finalLake;
                    }

                    //now we stamp our lake onto the mask
                    //trying not to place it over another

                    boolean placed = false;
                    int count = 0;
                    while (!placed) {
                        count++;
                        //pick some random x/y for the top corner
                        int xPlace = r.nextInt(x - xSize - 20) + 10;
                        int yPlace = r.nextInt(y - ySize - 20) + 10;

                        boolean canPlace = true;
                        for (int i = xPlace; i < xPlace + xSize; i++) {
                            for (int j = yPlace; j < yPlace + ySize; j++) {
                                if (finalLake[i - xPlace][j - yPlace] == 1) {
                                    if (archArray[i][j] == 1) {
                                        canPlace = false;
                                    }
                                }
                            }
                        }

                        if (w == WorldShape.ISLANDPLUS && count > 4){
                            canPlace = true;
                        }

                        if (canPlace){
                            for (int i = xPlace; i < xPlace + xSize; i++) {
                                for (int j = yPlace; j < yPlace + ySize; j++) {
                                    if (finalLake[i - xPlace][j - yPlace] == 1) {
                                        archArray[i][j] = 1;
                                    }
                                }
                            }
                            placed = true;
                        }

                        if (w== WorldShape.ARCHIPELAGO && count > 9){
                            placed = true;
                        }
                    }

                }

                //now use the mask to make oceans
                for (int i = 0; i < x; i++){
                    for (int j = 0; j < y; j++){
                        if (archArray[i][j] == 0){
                            TerrainCell c = (TerrainCell)map.get(i).get(j);
                            map.get(i).set(j, new TerrainCell(c.getId(), Terrain.OCEAN));
                        }
                    }
                }


        }

        terrain = true;
    }

    public void generateBeaches() {

        //this was only relevant before lakes so not needed now
        /*
        if (shape == WorldShape.LANDLOCKED) {
            //a landlocked map doesn't have ocean so no beaches
            return;
        }
        */

        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {

                TerrainCell current = (TerrainCell) map.get(i).get(j);
                if (current.getTerrainType() != Terrain.OCEAN && current.getTerrainType() != Terrain.JUNGLE && current.getTerrainType() != Terrain.LAKE && current.getTerrainType() != Terrain.RIVER) {

                    //check cells around for OCEAN
                    boolean beach = false;
                    for (int a = -1; a < 2; a++) {
                        for (int b = -1; b < 2; b++) {
                            if (i + a >= 0 && j + b >= 0) {
                                if (i + a < x && j + b < y) {
                                    TerrainCell c = (TerrainCell) map.get(i + a).get(j + b);
                                    if (c.getTerrainType() == Terrain.OCEAN || c.getTerrainType() == Terrain.LAKE || c.getTerrainType() == Terrain.RIVER) {
                                        if (c.getTerrainType() == Terrain.RIVER){
                                            if (Math.abs(a) + Math.abs(b) < 2){ //rounds off corners
                                                beach = true;
                                            }
                                        } else {
                                            beach = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (beach) {
                        map.get(i).set(j, new TerrainCell(current.getId(), Terrain.BEACH));
                    }

                }
            }
        }
    }

    public void generateLakes() {

        if (shape == WorldShape.ARCHIPELAGO) {
            //an archipelago won't generate lakes nicely so we don't bother;
            return;
        }

        Integer num = r.nextInt(Math.round((x * y) / 10000) + 1);
        //make a random number of lakes
        for (int z = 0; z < num; z++) {
            Integer xSize = r.nextInt(x / 3) + 4;
            Integer ySize = r.nextInt(y / 3) + 4;

            //set the square to all water
            int[][] lake = new int[xSize][ySize];
            for (int i = 0; i < xSize; i++) {
                for (int j = 0; j < ySize; j++) {
                    lake[i][j] = 1;
                }
            }

            //randomly shave in from each side
            for (int j = 0; j < ySize; j++) {
                //for each row
                int in = r.nextInt(xSize / 4) + 1;
                for (int q = 0; q < in; q++) {
                    lake[q][j] = 0;
                }
                in = r.nextInt(xSize / 4) + 1;
                for (int q = 0; q < in; q++) {
                    lake[(xSize - 1) - q][j] = 0;
                }
            }
            for (int i = 0; i < xSize; i++) {
                //for each column
                int in = r.nextInt(ySize / 4) + 1;
                for (int q = 0; q < in; q++) {
                    lake[i][q] = 0;
                }
                in = r.nextInt(ySize / 4) + 1;
                for (int q = 0; q < in; q++) {
                    lake[i][(ySize - 1) - q] = 0;
                }
            }

            //use a 'Game of Life' style rule to smooth edges
            //ground with 3 neighbouring water becomes water
            //water with less that 2 neighbouring ground becomes ground
            //we need to do this multiple times to deal with 'spiky' looking lakes on larger maps

            int times = Math.max(xSize, ySize) / 10;
            int[][] finalLake = new int[xSize][ySize];
            for (int q = 0; q < times; q++) {
                finalLake = new int[xSize][ySize];
                for (int i = 1; i < xSize - 1; i++) {
                    for (int j = 1; j < ySize - 1; j++) {
                        Integer count = 0;
                        //count neighbouring water (up, down, left, right)
                        if (lake[i + 1][j] == 1) {
                            count++;
                        }
                        if (lake[i][j + 1] == 1) {
                            count++;
                        }
                        if (lake[i - 1][j] == 1) {
                            count++;
                        }
                        if (lake[i][j - 1] == 1) {
                            count++;
                        }
                        if (lake[i][j] == 1) {
                            if (count < 3) {
                                finalLake[i][j] = 0;
                            } else {
                                finalLake[i][j] = 1;
                            }
                        } else {
                            if (count > 2) {
                                finalLake[i][j] = 1;
                            } else {
                                finalLake[i][j] = 0;
                            }
                        }
                    }
                }
                lake = finalLake;
            }

            //'stamp' the lake onto the map ensuring it doesn't touch the ocean
            boolean placed = false;
            int count = 0;
            while (!placed) {
                //pick some random x/y for the top corner;
                count++;

                int xPlace = r.nextInt(x - xSize - 20) + 10;
                int yPlace = r.nextInt(y - ySize - 20) + 10;
                //check for Ocean contact
                boolean canplace = true;
                for (int i = xPlace; i < xPlace + xSize; i++) {
                    for (int j = yPlace; j < yPlace + ySize; j++) {
                        TerrainCell c = (TerrainCell) map.get(i).get(j);
                        if (finalLake[i - xPlace][j - yPlace] == 1) {
                            if (c.getTerrainType() == Terrain.OCEAN || c.getTerrainType() == Terrain.DESERT) {
                                canplace = false;
                            }
                        }
                    }
                }

                //if we can place do it
                if (canplace) {
                    for (int i = xPlace; i < xPlace + xSize; i++) {
                        for (int j = yPlace; j < yPlace + ySize; j++) {
                            if (finalLake[i - xPlace][j - yPlace] == 1) {
                                TerrainCell c = (TerrainCell) map.get(i).get(j);
                                map.get(i).set(j, new TerrainCell(c.getId(), Terrain.LAKE));
                            }
                        }
                    }
                    placed = true;
                }
                if (count > 9) {
                    placed = true;
                }
            }


        }
    }

    public void generateRivers() {

        Integer num = Math.round((x * y) / 40000) + 1;
        //make a certain number of rivers
        for (int i = 0; i < num; i++) {
            //a river should generate from a lake or less often an edge
            boolean picked = false;
            int pickedX = 0;
            int pickedY = 0;
            int count = 0;

            //define direction up here so it can be dicided if on the edge of the map
            int dir = 0;

            while (!picked){

                //pick a random spot
                int randX = (r.nextInt(x));
                int randY = (r.nextInt(y));

                //check if the spot is a river
                TerrainCell c = (TerrainCell) map.get(randX).get(randY);
                if (c.getTerrainType().equals(Terrain.LAKE)){
                    pickedX = randX;
                    pickedY = randY;
                    picked = true;
                    break;
                }

                //if we're on a nation or landlocked map try a random spot on the edge after a few tries for a lake
                if ((shape == WorldShape.LANDLOCKED || shape ==WorldShape.NATION) && count > 15){
                    int rand = (r.nextInt(4));
                    TerrainCell cell;
                    switch(rand){
                        case 0:
                            cell = (TerrainCell) map.get(randX).get(0);
                            if (!(cell.getTerrainType().equals(Terrain.OCEAN))){
                                pickedX = randX;
                                pickedY = 0;
                                picked = true;
                                dir = (rand + 2) % 4; //opposite direction
                            }
                            break;
                        case 1:
                            cell = (TerrainCell) map.get(x-1).get(randY);
                            if (!(cell.getTerrainType().equals(Terrain.OCEAN))){
                                pickedX = x-1;
                                pickedY = randY;
                                picked = true;
                                dir = (rand + 2) % 4; //opposite direction
                            }
                            break;
                        case 2:
                            cell = (TerrainCell) map.get(randX).get(y-1);
                            if (!(cell.getTerrainType().equals(Terrain.OCEAN))){
                                pickedX = randX;
                                pickedY = y-1;
                                picked = true;
                                dir = (rand + 2) % 4; //opposite direction
                            }
                            break;
                        case 3:
                            cell = (TerrainCell) map.get(0).get(randY);
                            if (!(cell.getTerrainType().equals(Terrain.OCEAN))){
                                pickedX = 0;
                                pickedY = randY;
                                picked = true;
                                dir = (rand + 2) % 4; //opposite direction
                            }
                            break;
                    }
                }

                //don't check infinitely
                if (count > 200){
                    break;
                }

                count++;
            }

            if (picked){
                //now we build the river
                /*
                    The river is a random walk that is more likely to keep going straight then change direction
                    it can only change direction relatively (left or right).
                 */

                int currX = pickedX;
                int currY = pickedY;

                //keep track of whether or not we've left a lake
                boolean leftLake = false;

                //set the first tile
                //need some logic to decide initial direction due to maybe being on a map edge
                TerrainCell c = (TerrainCell) map.get(currX).get(currY);
                if (c.getTerrainType() != Terrain.LAKE) {
                    //we started on the edge of the map
                    map.get(currX).set(currY, new TerrainCell(c.getId(), Terrain.RIVER));
                    leftLake = true;
                    //direction is already decided by random point selection
                } else {
                    //started in a lake
                    //decide our initial direction
                    dir = r.nextInt(4); // 0 up   1 east   2 down   3 west
                }

                boolean running = true;
                int initial = dir; //river shouldn't go back on itself

                while (running){

                    //move to next cell

                    switch(dir){
                        case 0:
                            currY--;
                            break;
                        case 1:
                            currX++;
                            break;
                        case 2:
                            currY++;
                            break;
                        case 3:
                            currX--;
                            break;
                        default:
                            System.err.println(dir);
                    }

                    //make changes based on what/where our next cell is
                    TerrainCell t = (TerrainCell) map.get(currX).get(currY);
                    boolean set = true;
                    if (currX == 0 || currX == x-1 || currY == 0 || currY == y-1){
                        //we're on the edge of the map, stop
                        running = false;
                    } else if (t.getTerrainType() == Terrain.OCEAN){
                        //we've hit an ocean, we can stop
                        running = false;
                        set = false;
                    } else if (t.getTerrainType() == Terrain.RIVER){
                        //we've hit another river, stop
                        running = false;
                        set = false;
                    } else if (t.getTerrainType() == Terrain.LAKE){
                        //we've hit a lake, check if it's the initial lake
                        if (leftLake){
                            //we can stop
                            running = false;
                            set = false;
                        }
                    } else {
                        //we're on a land based terrain cell, we can assume (if we haven't already) that we've left our starting lake
                        leftLake = true;
                    }

                    if (set && leftLake){
                        //set our terraincell to RIVER
                        map.get(currX).set(currY, new TerrainCell(t.getId(), Terrain.RIVER));
                    }

                    //decide whether or not to change direction
                    if (running){
                        int z = r.nextInt(10); //1 in 5 change to change
                        if (z == 0){
                            //we're changing
                            //decide if it's left or right
                            int way = r.nextInt(2);
                            if (way == 0){
                                //right
                                dir = (dir + 1) % 4;
                            } else if (way == 1){
                                //left
                                dir = (dir + 3) % 4;
                            }
                            if (dir == (initial + 2) % 4){
                                dir = initial;
                            }

                        }
                    }

                }


            }
        }

    }

    public Cell cellAt(Integer x, Integer y) {
        return map.get(x).get(y);
    }

    public List<List<Cell>> getMap() {
        return map;
    }

    public Integer getXSize() {
        return x;
    }

    public Integer getYSize() {
        return y;
    }

    /*
        OUTPUTS
     */

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        String result;
        try {
            mapper.writeValue(new File("../TerrainView/data/terrain.json"), this);
            result = "success";
        } catch (Exception e) {
            System.err.println(e.getMessage() + "\n");
            result = "error";
        }
        return result;
    }

    public BufferedImage toImage(int cWidth, int cHeight){
        BufferedImage bi = new BufferedImage(x * cWidth, y * cHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();


        for (int i = 0; i < x; i++){
            for (int j = 0; j < y; j++){

                TerrainCell cell = (TerrainCell)map.get(i).get(j);


                Color color = Color.black;
                switch (cell.getTerrainType()){
                    case DESERT:
                        color = new Color(255, 243, 63);
                        break;
                    case SAVANNAH:
                        color = new Color(149, 156, 40);
                        break;
                    case PLAIN:
                        color = new Color(67, 226, 29);
                        break;
                    case FOREST:
                        color = new Color(50, 167, 22);
                        break;
                    case JUNGLE:
                        color = new Color(10, 99, 0);
                        break;
                    case OCEAN:
                        color = new Color(24, 50, 99);
                        break;
                    case BEACH:
                        color = new Color(255, 244, 182);
                        break;
                    case LAKE:
                        color = new Color(4, 67, 207);
                        break;
                    case RIVER:
                        color = new Color(10, 130, 255);
                        break;
                }

                g.setPaint(color);

                Rectangle rec = new Rectangle(i * cWidth, j * cHeight, cWidth, cHeight);

                g.fill(rec);
            }
        }


        return bi;
    }


}
