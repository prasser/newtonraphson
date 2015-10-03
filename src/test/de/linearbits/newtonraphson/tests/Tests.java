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
import de.linearbits.newtonraphson.Function;
import de.linearbits.newtonraphson.Function2D;
import de.linearbits.newtonraphson.Function2DUtil;
import de.linearbits.newtonraphson.NewtonRaphson2D;
import de.linearbits.newtonraphson.Pair;
import de.linearbits.newtonraphson.SquareMatrix2D;
import de.linearbits.newtonraphson.Vector2D;

/**
 * Some very basic tests
 * @author Fabian Prasser
 */
public class Tests {

    /**
     * Entry point
     * @param args
     */
    public static void main(String[] args) {
        
        // First object function : 3 * x^2 + 2 * y^2 - 35 = 0
        // Second object function: 4 * x^2 - 3 * y^2 - 24 = 0
        //
        // This system has four solutions: (+-3, +-2)
        
        /* ****************************
         *  Solve without derivatives *
         ******************************/

        Function2D object1 = getObjectFunction1();
        Function2D object2 = getObjectFunction2();
        
        NewtonRaphson2D solver = new NewtonRaphson2D(object1, object2)
                                                     .accuracy(1e-6)
                                                     .iterationsPerTry(1000)
                                                     .iterationsTotal(100000);
        
        solve(object1, object2, solver, 1000000);

        /* *************************************************
         *  Solve without derivatives but with constraints *
         ***************************************************/

        // We want a solution in the negative range
        Constraint2D constraint = new Constraint2D(){ 
            public Boolean evaluate(Vector2D input) { 
                return input.x < 0 && input.y < 0; 
            }
        };
        
        solver = new NewtonRaphson2D(object1, object2, constraint)
                                     .accuracy(1e-6)
                                     .iterationsPerTry(1000)
                                     .iterationsTotal(100000);
        
        solve(object1, object2, solver, 1000000);
        
        /* *************************
         *  Solve with derivatives *
         ***************************/
        
        Function2D derivative11 = getDerivativeFunction11();
        Function2D derivative12 = getDerivativeFunction12();
        Function2D derivative21 = getDerivativeFunction21();
        Function2D derivative22 = getDerivativeFunction22();

        solver = new NewtonRaphson2D(object1, object2, 
                                     derivative11, derivative12,
                                     derivative21, derivative22)
                                     .accuracy(1e-6)
                                     .iterationsPerTry(1000)
                                     .iterationsTotal(100000);
        
        Function2DUtil util = new Function2DUtil();
        System.out.println("\nChecking derivatives:");
        System.out.println("Is derivative: " + util.isDerivativeFunction1(object1, derivative11, 0.01, 100, 0.001, 0.1d, 0.01d));
        System.out.println("Is derivative: " + util.isDerivativeFunction2(object1, derivative12, 0.01, 100, 0.001, 0.1d, 0.01d));
        System.out.println("Is derivative: " + util.isDerivativeFunction1(object2, derivative21, 0.01, 100, 0.001, 0.1d, 0.01d));
        System.out.println("Is derivative: " + util.isDerivativeFunction2(object2, derivative22, 0.01, 100, 0.001, 0.1d, 0.01d));
        
        solve(object1, object2, solver, 1000000);
        
        /* *****************************
         *  Solve with master function *
         *******************************/
        
        Function<Vector2D, Pair<Vector2D, SquareMatrix2D>> master = getMasterFunction();
        solver = new NewtonRaphson2D(master)
                                     .accuracy(1e-6)
                                     .iterationsPerTry(1000)
                                     .iterationsTotal(100000);
        
        solve(object1, object2, solver, 1000000);
    }
    
    /**
     * Returns an implementation of the first derivative of the first object function:<br>
     * 6 * x
     * @return
     */
    private static Function2D getDerivativeFunction11() {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                return 6d * input.x;
            }
        };
    }

    /**
     * Returns an implementation of the second derivative of the first object function:<br>
     * 4 * y
     * @return
     */
    private static Function2D getDerivativeFunction12() {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                return 4d * input.y;
            }
        };
    }
    
    /**
     * Returns an implementation of the first derivative of the second object function:<br>
     * 8 * x
     * @return
     */
    private static Function2D getDerivativeFunction21() {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                return 8d * input.x;
            }
        };
    }

    /**
     * Returns an implementation of the second derivative of the second object function:<br>
     * - 6 * y
     * @return
     */
    private static Function2D getDerivativeFunction22() {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                return - 6d * input.y;
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

            private final SquareMatrix2D                 derivatives  = new SquareMatrix2D();
            // Prepare result objects
            private final Vector2D                       object       = new Vector2D();
            private final Pair<Vector2D, SquareMatrix2D> result       = new Pair<Vector2D, SquareMatrix2D>(object, derivatives);

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
    }
    
    /**
     * Returns an implementation of the first object function:<br>
     * 3 * x^2 + 2 * y^2 - 35 = 0 
     * @return
     */
    private static Function2D getObjectFunction1() {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                return 3d * input.x * input.x + 2d * input.y * input.y - 35d;
            }
        };
    }
    
    /**
     * Returns an implementation of the second object function:<br>
     * 4 * x^2 - 3 * y^2 - 24 = 0
     * @return
     */
    private static Function2D getObjectFunction2() {
        return new Function2D() {
            public Double evaluate(Vector2D input) {
                return 4d * input.x * input.x - 3d * input.y * input.y - 24d;
            }
        };
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
        
        DecimalFormat format = new DecimalFormat("0.000000");
        double time = (System.currentTimeMillis() - start) / (double)repetitions;
        System.out.println("Time    : " + format.format(time));
        System.out.println(solver.getMeasures());
        System.out.println("Result  : " + result);
    }
}
