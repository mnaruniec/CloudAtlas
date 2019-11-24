package pl.edu.mimuw.cloudatlas.fetcher;


public abstract class Mean {
	public abstract Double eval(double[] input);

	public static final Mean ARITHMETIC_MEAN = new Mean() {
		@Override
		public Double eval(double[] input) {
			if (input == null || input.length == 0) {
				return null;
			}
			double result = 0;
			for (double el: input) {
				result += el;
			}
			return result / input.length;
		}
	};

	public static final Mean GEOMETRIC_MEAN = new Mean() {
		@Override
		public Double eval(double[] input) {
			if (input == null || input.length == 0) {
				return null;
			}
			double result = 1;
			for (double el: input) {
				result *= el;
			}
			return Math.pow(result, 1.0 / (double)(input.length));
		}
	};

	public static final Mean HARMONIC_MEAN = new Mean() {
		@Override
		public Double eval(double[] input) {
			if (input == null || input.length == 0) {
				return null;
			}
			double result = 0;
			for (double el: input) {
				result += (1.0 / el);
			}
			return input.length / result;
		}
	};

	public static final Mean MAX = new Mean() {
		@Override
		public Double eval(double[] input) {
			if (input == null || input.length == 0) {
				return null;
			}
			double result = input[0];
			for (int i = 1; i < input.length; i++) {
				result = Math.max(result, input[i]);
			}
			return result;
		}
	};

	public static final Mean MIN = new Mean() {
		@Override
		public Double eval(double[] input) {
			if (input == null || input.length == 0) {
				return null;
			}
			double result = input[0];
			for (int i = 1; i < input.length; i++) {
				result = Math.min(result, input[i]);
			}
			return result;
		}
	};
}
