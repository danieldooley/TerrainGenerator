package terraingen.world.cell;

/**
 * Created by dandooley on 18/02/16.
 */

public class TerrainCell implements Cell {

    private Terrain terrainType;
    private Long id;

    public TerrainCell(Long id, Terrain type){
        this.id = id;
        this.terrainType = type;
    }

    public Terrain getTerrainType(){
        return terrainType;
    }

    @Override
    public String getType() {
        return "terrain";
    }

    @Override
    public Long getId() {
        return null;
    }

    public Boolean equals(Cell c){
        return c.getId().equals(id);
    }

}
