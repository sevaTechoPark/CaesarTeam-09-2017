package technoPark.mechanics.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;


@SuppressWarnings("PublicField")
public class Coords {

    public Coords(@JsonProperty("x") double x, @JsonProperty("y") double y) {
        this.x = x;
        this.y = y;
    }

    public final double x;
    public final double y;

    @Override
    public String toString() {
        return '{' +
                "x=" + x +
                ", y=" + y +
                '}';
    }
    @SuppressWarnings("NewMethodNamingConvention")
    @NotNull
    public static Coords of(double x, double y) {
        return new Coords(x, y);
    }
}
