package at.haha007.edenclient.utils.area;

import at.haha007.edenclient.render.*;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
public class TorusAreaRenderer implements Renderer {
    private final LineRenderer line = new LineRenderer();
    private final FaceRenderer face = new FaceRenderer();

    private final List<Line> lines = new ArrayList<>();
    private final List<Quad> faces = new ArrayList<>();

    public TorusAreaRenderer(Vec3 center, double majorRadius, double minorRadius, int majorSteps, int minorSteps) {
        face.color(new EdenColor(1,0,1,0.1f));
        Vec3[][] points = new Vec3[majorSteps][minorSteps];
        for (int i = 0; i < majorSteps; i++) {
            double majorAngle = 2 * Math.PI * i / majorSteps;
            double cosMajor = Math.cos(majorAngle);
            double sinMajor = Math.sin(majorAngle);
            for (int j = 0; j < minorSteps; j++) {
                double minorAngle = 2 * Math.PI * j / minorSteps;
                double ringRadius = majorRadius + minorRadius * Math.cos(minorAngle);
                points[i][j] = new Vec3(
                        center.x + ringRadius * cosMajor,
                        center.y + minorRadius * Math.sin(minorAngle),
                        center.z + ringRadius * sinMajor
                );
            }
        }

        // Rings around the main axis
        for (int j = 0; j < minorSteps; j++) {
            for (int i = 0; i < majorSteps; i++) {
                lines.add(new Line(points[i][j], points[(i + 1) % majorSteps][j]));
            }
        }
        // Rings around the tube
        for (int i = 0; i < majorSteps; i++) {
            for (int j = 0; j < minorSteps; j++) {
                lines.add(new Line(points[i][j], points[i][(j + 1) % minorSteps]));
            }
        }
        // Tube surface
        for (int i = 0; i < majorSteps; i++) {
            int nextMajor = (i + 1) % majorSteps;
            for (int j = 0; j < minorSteps; j++) {
                int nextMinor = (j + 1) % minorSteps;
                faces.add(new Quad(
                        points[i][j],
                        points[nextMajor][j],
                        points[nextMajor][nextMinor],
                        points[i][nextMinor]
                ));
            }
        }
    }

    @Override
    public void render() {
        line.render(lines);
        face.render(faces);
    }
}
