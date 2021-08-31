package cambio.simulator.parsing.adapter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.ParsingException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import desmoj.core.dist.ContDistNormal;

/**
 * @author Lion Wagner
 */
public class NormalDistributionAdapter extends TypeAdapter<ContDistNormal> {

    private final MiSimModel model;

    public NormalDistributionAdapter(MiSimModel model) {

        this.model = model;
    }

    @Override
    public void write(JsonWriter out, ContDistNormal value) throws IOException {

    }

    @Override
    public ContDistNormal read(JsonReader in) throws IOException {

        JsonToken peeked = in.peek();
        if (peeked == JsonToken.STRING) {
            //Distribution format: [0-9]*[1-9](+[rightDEVIATION]-[leftDEVIATION])|(+-DEVIATION)
            String untrimmedValue = in.nextString();
            String value = untrimmedValue.trim();

            double mean;
            double deviationLeft;
            double deviationRight;

            Pattern p = Pattern.compile("^(.+)\\+((?!-).+)-(.+)$");
            Matcher matcher = p.matcher(value);

            try {
                if (value.contains("+-")) {
                    String[] splittedValues = value.split("\\+-");
                    mean = Double.parseDouble(splittedValues[0]);
                    deviationLeft = deviationRight = Double.parseDouble(splittedValues[1]);

                } else if (matcher.find()) {
                    mean = Double.parseDouble(matcher.group(1));
                    deviationLeft = Double.parseDouble(matcher.group(2));
                    deviationRight = Double.parseDouble(matcher.group(3));
                } else {
                    mean = Double.parseDouble(value);
                    deviationLeft = deviationRight = 0;
                }
            } catch (NumberFormatException | NullPointerException e) {
                throw new ParsingException(
                    String.format(
                        "Could not parse distribution expression \"%s\". It should have the from of [mean]+-[deviation] or [mean]+[leftDev]-[rightDev]",
                        untrimmedValue), e);
            }

            return new ContDistNormal(model, "NormalDist", mean, deviationLeft, deviationRight, false, false);

        } else if (peeked == JsonToken.NUMBER) {
            return new ContDistNormal(model, "NormalDist", in.nextDouble(), 0, false, false);
        }
        return null;
    }
}
