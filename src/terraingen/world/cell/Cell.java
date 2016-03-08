package terraingen.world.cell;

/**
 * Created by dandooley on 18/02/16.
 */
public interface Cell {

    public String getType();

    public Long getId();

    public Boolean equals(Cell c);

}
