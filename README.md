# Newton's method in Java 

This project provides a very simple implementation of the [Newton-Raphson method](http://en.wikipedia.org/wiki/Newton%27s_method) 
for solving *bivariate non-linear equation systems*. 

## Example

As an example, we solve the following equation system:

```
3 * x^2 + 2 * y^2 - 35 = 0
4 * x^2 - 3 * y^2 - 24 = 0
```

This system has four solutions: (+-3, +-2)

### Basic solution

Implementing the object functions is straightforward. The first function:

```Java
Function2D object1 = new Function2D() {
	public Double evaluate(Vector2D input) {
		return 3d * input.x * input.x + 2d * input.y * input.y - 35d;
	}
}
```

And the second function:

```Java
Function2D object2 = new Function2D() {
	public Double evaluate(Vector2D input) {
		return 4d * input.x * input.x - 3d * input.y * input.y - 24d;
	}
}
```

We may now solve the equation system with:

```Java
Vector2D result = new NewtonRaphson2D(object1, object2)
                                     .accuracy(1e-6)
                                     .iterationsPerTry(1000)
                                     .iterationsTotal(100000)
                                     .solve();
```

This very simple variant of the solver will use the finite difference method for approximating the derivatives. The result is:

Measure    | Value
---------- | -------------
Time       | 0.0006 [ms]
Tries      | 1
Iterations | 6
Quality    | 0.999999

Without further constraints, the solver will return the solution (3,2). We may specify constraints to
find a solution with negative parameters:

```Java
Constraint2D constraint = new Constraint2D(){ 
	public Boolean evaluate(Vector2D input) { return input.x < 0 && input.y < 0; } 
};
                    
solver = new NewtonRaphson2D(object1, object2, constraint)...
```

### First enhanced solution

We can compute the partial derivatives of our object functions:

```
d/dx(f1) =   6 * x
d/dy(f1) =   4 * y
d/dx(f2) =   8 * x
d/dy(f2) = - 6 * y
```

And provide instances of ```Function2D``` to implement these derivatives:

```Java
Function2D derivative11 = new Function2D() {
	public Double evaluate(Vector2D input) {
		return 6d * input.x;
	}
}
```

We can also run a simple check, to compare our explicit forms with the results of the finite difference method to make sure that
we didn't make any mistakes:

```Java
Function2DUtil util = new Function2DUtil(1e-6);
util.isDerivativeFunction1(object1, derivative11, 0.01, 100, 0.001, 0.1d, 0.01d);
util.isDerivativeFunction2(object1, derivative12, 0.01, 100, 0.001, 0.1d, 0.01d);
```

Finally, we run the solver:

```Java
solver = new NewtonRaphson2D(object1, object2, 
							 derivative11, derivative12, 
							 derivative21, derivative22);
```

Using the explicit forms of the derivatives will speed up our computations, in this simple example by a factor of about 20%:

Measure    | Value
---------- | -------------
Time       | 0.005 [ms]
Tries      | 1
Iterations | 6
Quality    | 0.999999

### Second enhanced solution

We can further speed up the solving process by realizing that our object functions share some code. 
We can therefore implement a "master" function that evaluates the object functions and the partial
derivatives at the same time:

```Java
return new Function<Vector2D, Pair<Vector2D, SquareMatrix2D>>() {

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
	
  		// Prepare
  		double xSquare = input.x * input.x;
		double ySquare = input.y * input.y;
                
		// Compute
		object.x = 3d * xSquare + 2d * ySquare - 35d;
		object.y = 4d * xSquare - 3d * ySquare - 24d;
		derivatives.x1 = + 6d * input.x;
		derivatives.x2 = + 4d * input.y;
		derivatives.y1 = + 8d * input.x;
		derivatives.y2 = - 6d * input.y;
                
		// Return
		return result;
	}
};
```

This will speed up our computations by an additional 20%:

Measure    | Value
---------- | -------------
Time       | 0.0004 [ms]
Tries      | 1
Iterations | 6
Quality    | 0.999999

The complete implementation of this example can be found [here](https://github.com/prasser/newtonraphson/blob/master/src/test/de/linearbits/newtonraphson/tests/Tests.java)

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