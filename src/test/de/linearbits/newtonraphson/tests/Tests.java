/*
 * Copyright 2015 Fabian Prasser
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.linearbits.newtonraphson.tests;

import java.text.DecimalFormat;

import de.linearbits.newtonraphson.Constraint2D;
import de.linearbits.newtonraphson.Derivation;
import de.linearbits.newtonraphson.Function;
import de.linearbits.newtonraphson.Function2D;
import de.linearbits.newtonraphson.Function2DUtil;
import de.linearbits.newtonraphson.NewtonRaphson2D;
import de.linearbits.newtonraphson.Pair;
import de.linearbits.newtonraphson.PolyGamma;
import de.linearbits.newtonraphson.SquareMatrix2D;
import de.linearbits.newtonraphson.Vector2D;

/**
 * Some very basic tests
 * @author Fabian Prasser
 */
public class Tests {
    
    /** N*/
    private static final int N = 123412;

    /**
     * Entry point
     * @param args
     */
    public static void main(String[] args) {
        
        // First object function : sum[b/(a+i),i=1 to n]
        // Second object function: sum[1/(a+b+i)^2,i=1 to n]
        
        /* **********************************************************
         *  Solve with iterative implementations and no derivatives *
         ************************************************************/

        Function2D object1 = getObjectFunction1Iterative(N);
        Function2D object2 = getObjectFunction2Iterative(N);
        
        NewtonRaphson2D solver = new NewtonRaphson2D(object1, object2)
                                                     .accuracy(1e-6)
                                                     .iterationsPerTry(1000)
                                                     .iterationsTotal(100000);
        
        solve(object1, object2, solver, 10);

        /* ***********************************************************************
         *  Solve with iterative implementations and, derivatives and constraints*
         *************************************************************************/

        object1 = getObjectFunction1Iterative(N);
        object2 = getObjectFunction2Iterative(N);

        // d1/da and d1/db are only defined for a > -1
        Constraint2D constraint1 = new Constraint2D(){ 
            public Boolean evaluate(Vector2D input) { return input.x > -1; } };
            
        // a2/da and d2/db are only defined for a + b > -1
        Constraint2D constraint2 = new Constraint2D(){ 
            public Boolean evaluate(Vector2D input) { return input.x + input.y > -1; } };
        
        solver = new NewtonRaphson2D(object1, object2, constraint1, constraint2)
                                     .accuracy(1e-6)
                                     .iterationsPerTry(1000)
                                     .iterationsTotal(100000);
        
        solve(object1, object2, solver, 10);

        
        /* *******************************************************
         *  Solve with closed implementations and no derivatives *
         *********************************************************/
        
        object1 = getObjectFunction1Closed(N);
        object2 = getObjectFunction2Closed(N);
        
        Function2DUtil util = new Function2DUtil(1e-6);
        
        System.out.println("\nChecking first object function:");
        System.out.println("Is same [0,   100]: " + util.isSameFunction1(object1, getObjectFunction1Iterative(N), 0, 100, 0.1d, 1, 0.1d));
        System.out.println("Is same [0, 0.001]: " + util.isSameFunction1(object1, getObjectFunction1Iterative(N), 0, 1, 0.001d, 1, 0.1d));
        System.out.println("Is same [0,   100]: " + util.isSameFunction2(object1, getObjectFunction1Iterative(N), 0, 100, 0.1d, 1, 0.1d));
        System.out.println("Is same [0, 0.001]: " + util.isSameFunction2(object1, getObjectFunction1Iterative(N), 0, 1, 0.001d, 1, 0.1d));
        
        System.out.println("\nChecking second object function:");
        System.out.println("Is same [0,   100]: " + util.isSameFunction1(object2, getObjectFunction2Iterative(N), 0, 100, 0.1d, 1, 0.1d));
        System.out.println("Is same [0, 0.001]: " + util.isSameFunction1(object2, getObjectFunction2Iterative(N), 0, 1, 0.001d, 1, 0.1d));
        System.out.println("Is same [0,   100]: " + util.isSameFunction2(object2, getObjectFunction2Iterative(N), 0, 100, 0.1d, 1, 0.1d));
        System.out.println("Is same [0, 0.001]: " + util.isSameFunction2(object2, getObjectFunction2Iterative(N), 0, 1, 0.001d, 1, 0.1d));
        
        solver = new NewtonRaphson2D(object1, object2)
                                     .accuracy(1e-6)
                                     .iterationsPerTry(1000)
                                     .iterationsTotal(100000);

        solve(object1, object2, solver, 100000);

        /* *********************************************************
         *  Solve with closed implementations with two derivatives *
         ***********************************************************/
        
        object1 = getObjectFunction1Closed(N);
        object2 = getObjectFunction2Closed(N);
        
        Derivation derivation = new Derivation(1e-6);
        
        Function2D derivative11 = getDerivativeFunction11Closed(N);
        Function2D derivative12 = getDerivativeFunction12Closed(N);
        Function2D derivative21 = derivation.derive1(object2);
        Function2D derivative22 = derivation.derive2(object2);
        
        System.out.println("\nChecking first derivative:");
        System.out.println("Is derivative [0,   100]: " + util.isDerivativeFunction1(object1, derivative11, 0, 100, 0.1d, 1, 0.1d));
        System.out.println("Is derivative [0, 0.001]: " + util.isDerivativeFunction1(object1, derivative11, 0, 1, 0.001d, 1, 0.1d));
        
        System.out.println("\nChecking second derivative:");
        System.out.println("Is derivative [0,   100]: " + util.isDerivativeFunction2(object1, derivative12, 0, 100, 0.1d, 1, 0.1d));
        System.out.println("Is derivative [0, 0.001]: " + util.isDerivativeFunction2(object1, derivative12, 0, 1, 0.001d, 1, 0.1d));
        
        solver = new NewtonRaphson2D(object1, object2,
                                     derivative11, derivative12, derivative21, derivative22)
                                     .accuracy(1e-6)
                                     .iterationsPerTry(1000)
                                     .iterationsTotal(100000);

        solve(object1, object2, solver, 1000000);
        

        /* *****************************
         *  Solve with master function *
         *******************************/
        
        Function<Vector2D, Pair<Vector2D, SquareMatrix2D>> master = getMasterFunction(N);
        solver = new NewtonRaphson2D(master)
                                     .accuracy(1e-6)
                                     .iterationsPerTry(1000)
                                     .iterationsTotal(100000);
        
        solve(object1, object2, solver, 1000000);
    }
    
    /**
     * Solves and prints measures
     * @param object1
     * @param object2
     * @param solver
     */
    private static void solve(Function2D object1, Function2D object2, NewtonRaphson2D solver, int repetitions) {
        
        Vector2D result = null;
        long start = System.currentTimeMillis();
        for (int i=0; i< repetitions; i++) {
            result = solver.solve();
        }
        
        System.out.println("\nResults");
        System.out.println("*******");
        
        double time = (System.currentTimeMillis() - start) / (double)repetitions;
        System.out.println("Time    : " + time);
        
        double v1  = object1.evaluate(result);
        double v2  = object2.evaluate(result);
        double quality = (1.0d - Math.sqrt(v1*v1 + v2+v2))*100d;
        
        DecimalFormat format = new DecimalFormat("##0.000000");
        System.out.println(solver.getMeasures());
        System.out.println("Result  : " + result);
        System.out.println("Quality : " + format.format(quality) + "[%]");
    }

    /**
     * Returns an iterative implementation of the first object function
     * @param n
     * @return
     */
    private static Function2D getObjectFunction1Iterative(final int n) {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                double a = input.x, b = input.y, v = 0d;
                for (int i = 1; i <= n; i++) {
                    v += b / (a + i);
                }
                return v;
            }
        };
    }
    
    /**
     * Returns an iterative implementation of the second object function
     * @param n
     * @return
     */
    private static Function2D getObjectFunction2Iterative(final int n) {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                double ab = input.x + input.y, v = 0d;
                for (int i = 1; i <= n; i++) {
                    double t = ab + i;
                    v += 1d / (t * t);
                }
                return v;
            }
        };
    }

    /**
     * Returns a closed implementation of the first object function
     * @param n
     * @return
     */
    private static Function2D getObjectFunction1Closed(final int n) {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                double a = input.x, b = input.y;
                double v = b * (PolyGamma.digamma(a + n + 1.0d) - PolyGamma.digamma(a + 1.0d));
                return v;
            }
        };
    }
    
    /**
     * Returns a closed implementation of the second object function
     * @param n
     * @return
     */
    private static Function2D getObjectFunction2Closed(final int n) {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                double a = input.x, b = input.y;
                double v = PolyGamma.trigamma(a + b + 1.0d) - PolyGamma.trigamma(a + b + n + 1.0d);
                return v;
            }
        };
    }

    /**
     * Returns a closed implementation of the first derivative of the first object function
     * @param n
     * @return
     */
    private static Function2D getDerivativeFunction11Closed(final int n) {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                double a = input.x, b = input.y;
                double v = b * (PolyGamma.trigamma(a + n + 1.0d) - PolyGamma.trigamma(a + 1.0d));
                return v;
            }
        };
    }

    /**
     * Returns a closed implementation of the second derivative of the first object function
     * @param n
     * @return
     */
    private static Function2D getDerivativeFunction12Closed(final int n) {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                double a = input.x;
                double v = PolyGamma.digamma(a + n + 1.0d) - PolyGamma.digamma(a + 1.0d);
                return v;
            }
        };
    }
    
    /**
     * Returns the master function
     * @param n
     * @return
     */
    private static Function<Vector2D, Pair<Vector2D, SquareMatrix2D>> getMasterFunction(final int n) {
        
        // Return function
        return new Function<Vector2D, Pair<Vector2D, SquareMatrix2D>>() {

            // Use secant method for derivatives of the second object function
            private final Derivation                     derivation   = new Derivation(1e-6);
            private final Function2D                     derivative21 = derivation.derive1(getObjectFunction2Closed(n));
            private final Function2D                     derivative22 = derivation.derive2(getObjectFunction2Closed(n));

            // Prepare result objects
            private final Vector2D                       object       = new Vector2D();
            private final SquareMatrix2D                 derivatives  = new SquareMatrix2D();
            private final Pair<Vector2D, SquareMatrix2D> result       = new Pair<Vector2D, SquareMatrix2D>(object, derivatives);

            /**
             * Eval
             * @param input
             * @return
             */
            public Pair<Vector2D, SquareMatrix2D> evaluate(Vector2D input) {
                
                // Compute
                double a = input.x, b = input.y;
                double val0 = PolyGamma.digamma(a + n + 1.0d) - PolyGamma.digamma(a + 1.0d);
                double val1 = PolyGamma.trigamma(a + b + 1.0d) - PolyGamma.trigamma(a + b + n + 1.0d);
                double val2 = b * (PolyGamma.trigamma(a + n + 1.0d) - PolyGamma.trigamma(a + 1.0d));
                
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
    }
}
