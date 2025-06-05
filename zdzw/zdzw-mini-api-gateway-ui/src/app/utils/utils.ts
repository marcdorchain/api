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