package cambio.simulator.parsing.adapter;

import java.io.IOException;

import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.ParsingException;
import com.google.gson.stream.*;
import desmoj.core.dist.DiscreteDistConstant;
import desmoj.core.dist.NumericalDist;

/**
 * TODO:!!!!!! Adapter for parsing distributions from JSON into a
 * {@link NumericalDist}.
 *
 * <p>
 * Currently only accepts integer values.
 *
 * @author Sebastian Frank
 */
public class DiscreteDistributionAdapter extends MiSimModelReferencingTypeAdapter<NumericalDist<Integer>> {

	public DiscreteDistributionAdapter(MiSimModel model) {
		super(model);
	}

	@Override
	public void write(JsonWriter out, NumericalDist<Integer> value) throws IOException {

	}

	@Override
	public NumericalDist<Integer> read(JsonReader in) throws IOException {
		JsonToken peeked = in.peek();
		if (peeked == JsonToken.STRING) {
			String untrimmedValue = in.nextString();
			String value = untrimmedValue.trim();

			int constant = 1;
			try {
				constant = Integer.parseInt(value);

			} catch (NumberFormatException | NullPointerException e) {
				throw new ParsingException(
						String.format("Could not parse distribution expression \"%s\". It must be an integer value.",
								untrimmedValue),
						e);
			}

			return new DiscreteDistConstant<Integer>(model, "Constant", constant, false, false);

		} else if (peeked == JsonToken.NUMBER) {
			return new DiscreteDistConstant<Integer>(model, "Constant", in.nextInt(), false, false);
		}
		return null;
	}
}
