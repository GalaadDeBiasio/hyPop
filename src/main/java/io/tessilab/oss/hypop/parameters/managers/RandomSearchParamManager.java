/*
 * Copyright 2017 Tessi lab.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.tessilab.oss.hypop.parameters.managers;

import io.tessilab.oss.hypop.parameters.ParameterName;
import io.tessilab.oss.hypop.parameters.execution.ExecutionParameter;
import io.tessilab.oss.hypop.parameters.execution.ExecutionParametersSet;
import io.tessilab.oss.hypop.parameters.input.InputParameter;
import io.tessilab.oss.hypop.parameters.input.InputParametersSet;
import io.tessilab.oss.hypop.results.ProcessResult;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * A manager who simply gives random values for each parameter set. 
 * @author Andres BEL ALONSO
 */
public class RandomSearchParamManager extends ParametersManager{
    
    public static class Config extends ParametersManager.Config {
        
        private final int diffValues;
        
        public Config(int diffValues ) {
            this.diffValues = diffValues;
        }

        @Override
        protected ParametersManager build() {
            return new RandomSearchParamManager(this.getInputParameters(), diffValues);
        }
        
    }
    
    private final Map<ParameterName,List<ExecutionParameter>> possibleValues;
    private final Random randomGenerator;
    

    /**
     * 
     * @param params
     * @param diffValues : the number of different values that are going to be use
     */
    public RandomSearchParamManager(InputParametersSet params, int diffValues) {
        super(params);
        possibleValues = new TreeMap<>();
        for(InputParameter param : params.getAllParameters()) {
            possibleValues.put(param.getParameterName(), 
                    param.getPosibleValues(param.nbValues()==-1?diffValues:param.nbValues()));
        }
        randomGenerator = new Random(Calendar.getInstance().getTimeInMillis());
    }

    @Override
    public boolean hasJobsToExplore() {
        return true;
    }

    @Override
    public ExecutionParametersSet getNonBuildParameters() {
        List<ExecutionParametersSet> execParams = new LinkedList<>();
        for(InputParameter inputParam : this.params.getIndependentParameters()) {
            execParams.add(computeParameterAndSubParams(inputParam));
        }     
        return new ExecutionParametersSet(execParams,true);
    }

    @Override
    public int getJobsTodo() {
        return -1;
    }

    @Override
    public void canNotBeDoneNow(ExecutionParametersSet params) {
        // do not care
    }

    @Override
    public void updateObserver(ProcessResult obj) {
        // do not care 
    }
     
    public ExecutionParametersSet computeParameterAndSubParams(InputParameter<Object> param) {
        ExecutionParametersSet parametersSet = new ExecutionParametersSet();
        List<ExecutionParameter> values = this.possibleValues.get(param.getParameterName());
        ExecutionParameter execParam = values.get(randomGenerator.nextInt(values.size()));
        parametersSet.addParameter(execParam);
        //Add the subparameters
        List<ExecutionParametersSet> executionParam = new LinkedList<>();
        for(InputParameter subParam : param.getAssociatedSubParams(execParam.getValue()))  {
            executionParam.add(computeParameterAndSubParams(subParam));
        }
        executionParam.stream().forEach(e -> {
            e.getExecutionParameters().stream().forEach(f -> {
                parametersSet.addParameter(f);
            });
        });
        return parametersSet;
    }

    @Override
    public void notifyAsNotValidParameter(ExecutionParametersSet params) {
        // OK... we do not care about
    }
    
    
    
}
