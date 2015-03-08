# Newton's method in Java 

This project provides a very simple implementation of the [Newton-Raphson method](http://en.wikipedia.org/wiki/Newton%27s_method) 
for solving *bivariate non-linear equation systems*. 

## Example

As an example, we solve the following equation system (*a* and *b* are our variables) for *n = 123412*:

[![Equation system](https://raw.github.com/prasser/newtonraphson/master/media/system.png)](https://raw.github.com/prasser/newtonraphson/master/media/system.png)

### Basic solution

Implementing the object functions is straightforward. The first function:

```Java
Function2D object1 = new Function2D() {
	public Double evaluate(Vector2D input) {
		double a = input.x, b = input.y, v = 0d;
		for (int i = 1; i <= n; i++) {
			v += b / (a + i);
		}
		return v;
	}
}
```

And the second function:

```Java
Function2D object2 = new Function2D() {
	public Double evaluate(Vector2D input) {
		double ab = input.x + input.y, v = 0d;
		for (int i = 1; i <= n; i++) {
			double t = ab + i;
			v += 1d / (t * t);
		}
		return v;
	}
}
```

We may now solve the equation system with:

```Java
NewtonRaphson2D solver = new NewtonRaphson2D(object1, object2)
                                            .accuracy(1e-6)
                                            .iterationsPerTry(1000)
                                            .iterationsTotal(100000)
                                            .solve();
```

This very simple variant of the solver will use a secant method for approximating the derivatives. The result is:

Measure    | Value
---------- | -------------
Time       | 78.3 [ms]
Tries      | 1
Iterations | 19
Quality    | 99.864987 [%]

We know that the derivatives of the first object function are only defined for *a > -1* and that the derivatives of
the second object function are only defined for *a + b > -1*. So we may specify constraints:

```Java
Constraint2D constraint1 = new Constraint2D(){ 
	public Boolean evaluate(Vector2D input) { return input.x > -1; } 
};
            
Constraint2D constraint2 = new Constraint2D(){ 
	public Boolean evaluate(Vector2D input) { return input.x + input.y > -1; } 
};
        
solver = new NewtonRaphson2D(object1, object2, constraint1, constraint2)...
```

### First enhanced solution

We can use software like [Wolfram|Alpha](http://www.wolframalpha.com/)
to find closed forms of our object functions to speed up computations:

[![Formula1](https://raw.github.com/prasser/newtonraphson/master/media/formula1_closed.png)](https://raw.github.com/prasser/newtonraphson/master/media/formula1_closed.png)
[![Formula2](https://raw.github.com/prasser/newtonraphson/master/media/formula2_closed.png)](https://raw.github.com/prasser/newtonraphson/master/media/formula2_closed.png)

Now we can rewrite the first object function like this:

```Java
// Function2D object1Closed
double a = input.x, b = input.y;
return b * (PolyGamma.digamma(a + n + 1.0d) - PolyGamma.digamma(a + 1.0d));
```

And the second object function like this:

```Java
// Function2D object2Closed
double a = input.x, b = input.y;
return PolyGamma.trigamma(a + b + 1.0d) - PolyGamma.trigamma(a + b + n + 1.0d);
```
We can also run a simple check, to compare our new implementations with the old implementations to make sure that we
did'nt make any errors when converting or implementing the functions:

```Java
Function2DUtil util = new Function2DUtil(1e-6);
util.isSameFunction1(object1Closed, object1, 0, 100, 0.1d, 1, 0.1d);
util.isSameFunction1(object1Closed, object1, 0, 1, 0.001d, 1, 0.1d);
util.isSameFunction2(object1Closed, object1, 0, 100, 0.1d, 1, 0.1d);
util.isSameFunction2(object1Closed, object1, 0, 1, 0.001d, 1, 0.1d);
```

For example, the last method compares both functions for y-values in the range [0,1] with a stepping of 0.001 and
a fixed parameter *x = 1*. The functions are considered to be equal, when their results do not differ by more than 10% (0.1).

Using the closed forms of our object functions will greatly speed up our computations, by a factor of about 7000.
We can also see the effect of the approximate nature of our implementations of digamma and trigamma, as the 
algorithm converges more quickly (7 iterations instead of 19):

Measure    | Value
---------- | -------------
Time       | 0.01 [ms]
Tries      | 1
Iterations | 7
Quality    | 99.872340[%]
 
### Second enhanced solution

Again, we can use software like Wolfram|Alpha. In this case to find partial derivatives of our object functions:

[![Formula1](https://raw.github.com/prasser/newtonraphson/master/media/formula1_da.png)](https://raw.github.com/prasser/newtonraphson/master/media/formula1_da.png)
[![Formula2](https://raw.github.com/prasser/newtonraphson/master/media/formula1_db.png)](https://raw.github.com/prasser/newtonraphson/master/media/formula1_db.png)
[![Formula1](https://raw.github.com/prasser/newtonraphson/master/media/formula2_da.png)](https://raw.github.com/prasser/newtonraphson/master/media/formula2_da.png)
[![Formula2](https://raw.github.com/prasser/newtonraphson/master/media/formula2_db.png)](https://raw.github.com/prasser/newtonraphson/master/media/formula2_db.png)

With our approximations of digamma and trigamma, we can implement the first partial derivative *d/da* of the first object function like this:

```Java
// Function2D derivative11...
double a = input.x, b = input.y;
return b * (PolyGamma.trigamma(a + n + 1.0d) - PolyGamma.trigamma(a + 1.0d));
```

And the second partial derivative *d/db* of the first object function like this:

```Java
// Function2D derivative12...
double a = input.x, b = input.y;
return PolyGamma.digamma(a + n + 1.0d) - PolyGamma.digamma(a + 1.0d);
```

Again, we can run a simple check, to compare our explicit forms with the results of the secant method:

```Java
Function2DUtil util = new Function2DUtil(1e-6);        
util.isDerivativeFunction1(object1Closed, derivative11, 0, 100, 0.1d, 1, 0.1d);
util.isDerivativeFunction1(object1Closed, derivative11, 0, 1, 0.001d, 1, 0.1d);
util.isDerivativeFunction2(object1Closed, derivative12, 0, 100, 0.1d, 1, 0.1d);
util.isDerivativeFunction2(object1Closed, derivative12, 0, 1, 0.001d, 1, 0.1d);
```

For the two partial derivatives of our second object function we still use a secant method:

```Java
Derivation2D derivation = new Derivation2D(1e-6);
Function2D derivative21 = derivation.derive1(object2);
Function2D derivative22 = derivation.derive2(object2);
```

And run the solver:

```Java
solver = new NewtonRaphson2D(object1Closed, object2Closed, 
							 derivative11, derivative12, 
							 derivative21, derivative22).solve();
```

Using the explicit forms of some derivatives will again speed up our computations, this time by a factor of about 2:

Measure    | Value
---------- | -------------
Time       | 0.004 [ms]
Tries      | 1
Iterations | 7
Quality    | 99.872340[%]

### Third enhanced solution

We can further speed up the solving process by realizing that our object functions and one of our partial derivatives
share some code. We can therefore implement a "master" function that evaluates the object functions and the partial
derivatives at the same time:

```Java
return new Function<Vector2D, Pair<Vector2D, SquareMatrix2D>>() {

	// Use secant method for derivatives of the second object function
	Derivation2D                   derivation   = new Derivation2D(1e-6);
	Function2D                     derivative21 = derivation.derive1(object2Closed);
	Function2D                     derivative22 = derivation.derive2(object2Closed);

	// Prepare result objects
	Vector2D                       object       = new Vector2D();
    SquareMatrix2D                 derivatives  = new SquareMatrix2D();
    Pair<Vector2D, SquareMatrix2D> result       = new Pair<Vector2D, SquareMatrix2D>
    											  (object, derivatives);

	/**
	 * Eval
	 * @param input
	 * @return
	 */
	public Pair<Vector2D, SquareMatrix2D> evaluate(Vector2D input) {
	
		// Compute
		double a = input.x, b = input.y;
		double val0 = digamma(a + n + 1.0d) - digamma(a + 1.0d);
		double val1 = trigamma(a + b + 1.0d) - trigamma(a + b + n + 1.0d);
		double val2 = b * (trigamma(a + n + 1.0d) - trigamma(a + 1.0d));
        
        // Store
		object.x = b * val0;
		object.y = val1;
		derivatives.x1 = val2;
		derivatives.x2 = val0;
        derivatives.y1 = derivative21.evaluate(input);
        derivatives.y2 = derivative22.evaluate(input);
        
        // Return
        return result;
	}
};
```

This will speed up our computations by an additional 25%:

Measure    | Value
---------- | -------------
Time       | 0.003 [ms]
Tries      | 1
Iterations | 7
Quality    | 99.872340[%]

Download
------
A binary version (JAR file) is available for download [here](https://rawgithub.com/prasser/newtonraphson/master/jars/newtonraphson-0.0.1.jar).

The according Javadoc is available for download [here](https://rawgithub.com/prasser/newtonraphson/master/jars/newtonraphson-0.0.1-doc.jar). 

Documentation
------
Online documentation is [here](https://rawgithub.com/prasser/newtonraphson/master/doc/index.html).

License
------
Apache License 2.0