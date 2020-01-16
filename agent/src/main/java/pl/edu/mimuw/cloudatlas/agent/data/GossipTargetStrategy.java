package pl.edu.mimuw.cloudatlas.agent.data;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import pl.edu.mimuw.cloudatlas.agent.AgentConfig;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.util.ArrayList;
import java.util.List;

public abstract class GossipTargetStrategy {
	public static GossipTargetStrategy createStrategy(AgentConfig.GossipTargetStrategy strategyEnum) {
		switch (strategyEnum) {
			case UNIFORM_RANDOM:
				return UNIFORM_RANDOM;
			case EXP_RANDOM:
				return EXP_RANDOM;
			case UNIFORM_ROUND_ROBIN:
				return UNIFORM_ROUND_ROBIN;
			case EXP_ROUND_ROBIN:
				return EXP_ROUND_ROBIN;
			default:
				throw new IllegalArgumentException(
						"Gossip target strategy not registered for this config value. Shutting down.");
		}
	}


	public static GossipTargetStrategy UNIFORM_RANDOM = new GossipTargetStrategy() {
		@Override
		protected int getLevel(List<Integer> levels) {
			return levels.get(new UniformIntegerDistribution(0, levels.size() - 1).sample());
		}
	};

	public static GossipTargetStrategy EXP_RANDOM = new GossipTargetStrategy() {
		@Override
		protected int getLevel(List<Integer> levels) {
			int[] levelsArr = new int[levels.size()];
			double[] probs = new double[levels.size()];
			for (int i = 0; i < levels.size(); i++) {
				levelsArr[i] = levels.get(i);
				probs[i] = Math.pow(0.5, levelsArr[i] + 1);
			}
			return new EnumeratedIntegerDistribution(levelsArr, probs).sample();
		}
	};

	public static GossipTargetStrategy UNIFORM_ROUND_ROBIN = new GossipTargetStrategy() {
		private int nextLevel = 0;

		@Override
		protected int getLevel(List<Integer> levels) {
			int size = levels.size();
			int closest = levels.get(size - 1);
			if (closest < nextLevel) {
				nextLevel = levels.get(0);
			} else {
				for (int i = size - 1; i >= 0; i--) {
					if (levels.get(i) < nextLevel) {
						break;
					} else {
						closest = levels.get(i);
					}
				}
				nextLevel = closest;
			}
			return nextLevel++;
		}
	};

	public static GossipTargetStrategy EXP_ROUND_ROBIN = new GossipTargetStrategy() {

		@Override
		protected int getLevel(List<Integer> levels) {
			// TODO
			return 0;
		}
	};

	public ValueContact getNextTarget(List<List<List<ValueContact>>> levels) {
		if (levels.isEmpty()) {
			return null;
		}

		List<Integer> intlevels = new ArrayList<>();
		for (int i = 0; i < levels.size(); i++) {
			if (!levels.get(i).isEmpty()) {
				intlevels.add(i);
			}
		}

		if (intlevels.isEmpty()) {
			return null;
		}


		int level = getLevel(intlevels);
		System.out.println("Chose level " + level);
		return getFromLevel(levels.get(level));
	}

	protected abstract int getLevel(List<Integer> levels);

	protected ValueContact getFromLevel(List<List<ValueContact>> nodes) {
		UniformIntegerDistribution nodeDist = new UniformIntegerDistribution(0, nodes.size() - 1);
		List<ValueContact> node = nodes.get(nodeDist.sample());
		UniformIntegerDistribution contactDist = new UniformIntegerDistribution(0, node.size() - 1);
		return node.get(contactDist.sample());
	}
}
