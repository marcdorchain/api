/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
export const arrayEquals = (arrayA: any[], arrayB: any[]) => {
    // if the other array is a falsy value, return
    // if the argument is the same array, we can be sure the contents are same as well
    if(arrayB === arrayA)
        return true;
    if(!arrayA || !arrayB)
      return false;
    // compare lengths - can save a lot of time 
    if (arrayA.length != arrayB.length)
        return false;

    for (var i = 0, l=arrayA.length; i < l; i++) {
        // Check if we have nested arrays
        if (arrayA[i] instanceof Array && arrayB[i] instanceof Array) {
            // recurse into the nested arrays
            if (!arrayEquals(arrayA[i], arrayB[i]))
                return false;       
        }else if(arrayA[i] instanceof Object && arrayB[i] instanceof Object){
          if(!arrayEquals(Object.entries(arrayA[i]), Object.entries(arrayB[i]))){
            return false;
          }
        }           
        else if (arrayA[i] != arrayB[i]) { 
            // Warning - two different object instances will never be equal: {x:20} != {x:20}
            return false;   
        }           
    }       
    return true;
  }

//Source: https://stackoverflow.com/questions/1068834/object-comparison-in-javascript/6713782#6713782
export function objectEquals( x: any, y: any ) {
  if ( x === y ) return true;
    // if both x and y are null or undefined and exactly the same

  if ( ! ( x instanceof Object ) || ! ( y instanceof Object ) ) return false;
    // if they are not strictly equal, they both need to be Objects

  if ( x.constructor !== y.constructor ) return false;
    // they must have the exact same prototype chain, the closest we can do is
    // test there constructor.

  for ( var p in x ) {
    if ( ! x.hasOwnProperty( p ) ) continue;
      // other properties were tested using x.constructor === y.constructor

    if ( ! y.hasOwnProperty( p ) ) return false;
      // allows to compare x[ p ] and y[ p ] when set to undefined

    if ( x[ p ] === y[ p ] ) continue;
      // if they have the same strict value or identity then they are equal

    if ( typeof( x[ p ] ) !== "object" ) return false;
      // Numbers, Strings, Functions, Booleans must be strictly equal

    if ( ! objectEquals( x[ p ],  y[ p ] ) ) return false;
      // Objects and Arrays must be tested recursively
  }

  for ( p in y )
    if ( y.hasOwnProperty( p ) && ! x.hasOwnProperty( p ) )
      return false;
        // allows x[ p ] to be set to undefined

  return true;
}