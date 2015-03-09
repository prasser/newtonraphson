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

import de.linearbits.newtonraphson.Constraint2D;
import de.linearbits.newtonraphson.Derivation2D;
import de.linearbits.newtonraphson.Function;
import de.linearbits.newtonraphson.Function2D;
import de.linearbits.newtonraphson.Function2DUtil;
import de.linearbits.newtonraphson.NewtonRaphson2D;
import de.linearbits.newtonraphson.Pair;
import de.linearbits.newtonraphson.SquareMatrix2D;
import de.linearbits.newtonraphson.Vector2D;
import de.linearbits.polygamma.PolyGamma;

/**
 * Some very basic tests
 * @author Fabian Prasser
 */
public class Tests {

    /** N */
    private static final int    N        = 123456;
    /** C1 */
    private static final double C1       = 0.025911404898870522;
    /** C2 */
    private static final double C2       = 9.25018224693155E-5;
    /** Accuracy */
    private static final double ACCURACY = 0.01d;
    
    /**
     * Entry point
     * @param args
     */
    public static void main(String[] args) {
        
        // First object function : sum[b/(a+i),i=1 to n] - c1 
        // Second object function: sum[1/(a+b+i)^2,i=1 to n] - c2
        
        // With c1 = 0.025911404898870522
        // And c2 = 9.25018224693155E-5
        // And n = 123456;
        //
        // We have a solution at:
        // a = 10000
        // b = 0.01
        
        /* **********************************************************
         *  Solve with iterative implementations and no derivatives *
         ************************************************************/

        Function2D object1 = getObjectFunction1Iterative();
        Function2D object2 = getObjectFunction2Iterative();
        
        NewtonRaphson2D solver = new NewtonRaphson2D(object1, object2)
                                                     .accuracy(1e-6)
                                                     .iterationsPerTry(1000)
                                                     .iterationsTotal(100000);
        
        solve(object1, object2, solver, 100);

        /* ***********************************************************************
         *  Solve with iterative implementations and, derivatives and constraints*
         *************************************************************************/

        object1 = getObjectFunction1Iterative();
        object2 = getObjectFunction2Iterative();

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
        
        solve(object1, object2, solver, 100);

        
        /* *******************************************************
         *  Solve with closed implementations and no derivatives *
         *********************************************************/
        
        object1 = getObjectFunction1Closed();
        object2 = getObjectFunction2Closed();
        
        Function2DUtil util = new Function2DUtil(1e-6);
        
        System.out.println("\nChecking first object function:");
        System.out.println("Is same: " + util.isSameFunction1(object1, getObjectFunction1Iterative(), 0, 100, 0.1d, 1, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction1(object1, getObjectFunction1Iterative(), 0, 1, 0.001d, 1, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction2(object1, getObjectFunction1Iterative(), 0, 100, 0.1d, 1, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction2(object1, getObjectFunction1Iterative(), 0, 1, 0.001d, 1, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction2(object1, getObjectFunction1Iterative(), 0, 100, 0.1d, -1, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction2(object1, getObjectFunction1Iterative(), 0, 1, 0.001d, -1, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction1(object1, getObjectFunction1Iterative(), 10000000, 10000100, 0.1d, -1000, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction1(object1, getObjectFunction1Iterative(), 10000000, 10000001, 0.001d, -1000, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction2(object1, getObjectFunction1Iterative(), 10000000, 10000100, 0.1d, 1000, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction2(object1, getObjectFunction1Iterative(), 10000000, 10000001, 0.001d, 1000, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction2(object1, getObjectFunction1Iterative(), 10000000, 10000100, 0.1d, -1000, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction2(object1, getObjectFunction1Iterative(), 10000000, 10000001, 0.001d, -1000, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction2(object1, getObjectFunction1Iterative(), 0, 100, 0.1d, -100000000, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction2(object1, getObjectFunction1Iterative(), 0, 1, 0.001d, -100000000, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction1(object1, getObjectFunction1Iterative(), 10000000, 10000100, 0.1d, -100000000, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction1(object1, getObjectFunction1Iterative(), 10000000, 10000001, 0.001d, -100000000, ACCURACY));
        
        System.out.println("\nChecking second object function:");
        System.out.println("Is same: " + util.isSameFunction1(object2, getObjectFunction2Iterative(), 0, 100, 0.1d, 1, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction1(object2, getObjectFunction2Iterative(), 0, 1, 0.001d, 1, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction2(object2, getObjectFunction2Iterative(), 0, 100, 0.1d, 1, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction2(object2, getObjectFunction2Iterative(), 0, 1, 0.001d, 1, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction1(object2, getObjectFunction2Iterative(), 10000000, 10000100, 0.1d, -1000, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction1(object2, getObjectFunction2Iterative(), 10000000, 10000001, 0.001d, -1000, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction2(object2, getObjectFunction2Iterative(), 10000000, 10000100, 0.1d, -1000, ACCURACY));
        System.out.println("Is same: " + util.isSameFunction2(object2, getObjectFunction2Iterative(), 10000000, 10000001, 0.001d, -1000, ACCURACY));
        
        solver = new NewtonRaphson2D(object1, object2)
                                     .accuracy(1e-6)
                                     .iterationsPerTry(1000)
                                     .iterationsTotal(100000);

        solve(object1, object2, solver, 100000);

        /* *********************************************************
         *  Solve with closed implementations with two derivatives *
         ***********************************************************/
        
        object1 = getObjectFunction1Closed();
        object2 = getObjectFunction2Closed();
        
        Derivation2D derivation = new Derivation2D(1e-6);
        
        Function2D derivative11 = getDerivativeFunction11Closed();
        Function2D derivative12 = getDerivativeFunction12Closed();
        Function2D derivative21 = derivation.derive1(object2);
        Function2D derivative22 = derivation.derive2(object2);
        
        System.out.println("\nChecking first derivative:");
        System.out.println("Is derivative: " + util.isDerivativeFunction1(object1, derivative11, 0, 100, 0.1d, 1, ACCURACY));
        System.out.println("Is derivative: " + util.isDerivativeFunction1(object1, derivative11, 0, 1, 0.001d, 1, ACCURACY));
        
        System.out.println("\nChecking second derivative:");
        System.out.println("Is derivative: " + util.isDerivativeFunction2(object1, derivative12, 0, 100, 0.1d, 1, ACCURACY));
        System.out.println("Is derivative: " + util.isDerivativeFunction2(object1, derivative12, 0, 1, 0.001d, 1, ACCURACY));
        
        solver = new NewtonRaphson2D(object1, object2,
                                     derivative11, derivative12, derivative21, derivative22)
                                     .accuracy(1e-6)
                                     .iterationsPerTry(1000)
                                     .iterationsTotal(100000);

        solve(object1, object2, solver, 100000);

        /* *****************************
         *  Solve with master function *
         *******************************/
        
        Function<Vector2D, Pair<Vector2D, SquareMatrix2D>> master = getMasterFunction();
        solver = new NewtonRaphson2D(master)
                                     .accuracy(1e-6)
                                     .iterationsPerTry(1000)
                                     .iterationsTotal(100000);
        
        solve(object1, object2, solver, 100000);
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
        System.out.println(solver.getMeasures());
        System.out.println("Result  : " + result);
    }

    /**
     * Returns an iterative implementation of the first object function
     * @return
     */
    private static Function2D getObjectFunction1Iterative() {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                double a = input.x, b = input.y, v = 0d;
                for (int i = 1; i <= N; i++) {
                    v += b / (a + i);
                }
                return v - C1;
            }
        };
    }
    
    /**
     * Returns an iterative implementation of the second object function
     * @return
     */
    private static Function2D getObjectFunction2Iterative() {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                double ab = input.x + input.y, v = 0d;
                for (int i = 1; i <= N; i++) {
                    double t = ab + i;
                    v += 1d / (t * t);
                }
                return v - C2;
            }
        };
    }

    /**
     * Returns a closed implementation of the first object function
     * @return
     */
    private static Function2D getObjectFunction1Closed() {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                double a = input.x, b = input.y;
                double v = b * (PolyGamma.digamma(a + N + 1.0d) - PolyGamma.digamma(a + 1.0d));
                return v - C1;
            }
        };
    }
    
    /**
     * Returns a closed implementation of the second object function
     * @return
     */
    private static Function2D getObjectFunction2Closed() {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                double a = input.x, b = input.y;
                double v = PolyGamma.trigamma(a + b + 1.0d) - PolyGamma.trigamma(a + b + N + 1.0d);
                return v - C2;
            }
        };
    }

    /**
     * Returns a closed implementation of the first derivative of the first object function
     * @return
     */
    private static Function2D getDerivativeFunction11Closed() {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                double a = input.x, b = input.y;
                double v = b * (PolyGamma.trigamma(a + N + 1.0d) - PolyGamma.trigamma(a + 1.0d));
                return v;
            }
        };
    }

    /**
     * Returns a closed implementation of the second derivative of the first object function
     * @return
     */
    private static Function2D getDerivativeFunction12Closed() {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                double a = input.x;
                double v = PolyGamma.digamma(a + N + 1.0d) - PolyGamma.digamma(a + 1.0d);
                return v;
            }
        };
    }
    
    /**
     * Returns the master function
     * @return
     */
    private static Function<Vector2D, Pair<Vector2D, SquareMatrix2D>> getMasterFunction() {
        
        // Return function
        return new Function<Vector2D, Pair<Vector2D, SquareMatrix2D>>() {

            // Use secant method for derivatives of the second object function
            private final Derivation2D                     derivation   = new Derivation2D(1e-6);
            private final Function2D                     derivative21 = derivation.derive1(getObjectFunction2Closed());
            private final Function2D                     derivative22 = derivation.derive2(getObjectFunction2Closed());

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
                double val0 = PolyGamma.digamma(a + N + 1.0d) - PolyGamma.digamma(a + 1.0d);
                double val1 = PolyGamma.trigamma(a + b + 1.0d) - PolyGamma.trigamma(a + b + N + 1.0d);
                double val2 = b * (PolyGamma.trigamma(a + N + 1.0d) - PolyGamma.trigamma(a + 1.0d));
                
                // Store
                object.x = b * val0 - C1;
                object.y = val1 - C2;
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
