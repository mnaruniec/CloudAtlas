/**
 * Copyright (c) 2014, University of Warsaw
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypeCollection;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueList;

import java.util.List;
import java.util.stream.Collectors;

class ResultList extends Result {
    private final ValueList value;

    public ResultList(ValueList value) {
        if (value == null) {
            throw new InternalInterpreterException("Attempting to create ResultList with null ValueList");
        }
        this.value = value;
    }

    @Override
    protected ResultList binaryOperationTyped(BinaryOperation operation, ResultSingle right) {
        return unaryOperation(left -> operation.perform(left, right.getValue()));
    }

    @Override
    protected Result binaryOperationTyped(BinaryOperation operation, ResultList right) {
        throw new UnsupportedOperationException("No binary ops between two ResultLists.");
    }

    @Override
    protected Result binaryOperationTyped(BinaryOperation operation, ResultColumn right) {
        throw new UnsupportedOperationException("No binary ops between ResultList and ResultColumn.");
    }

    @Override
    public ResultList unaryOperation(UnaryOperation operation) {
        if (value.isNull()) {
            return new ResultList(new ValueList(null, TypePrimitive.NULL));
        }
        List<Value> result = value.getValue().stream().map(operation::perform).collect(Collectors.toList());
        return new ResultList(new ValueList(result, TypeCollection.computeElementType(result)));
    }

    @Override
    protected Result callMe(BinaryOperation operation, Result left) {
        return left.binaryOperationTyped(operation, this);
    }

    @Override
    public Value getValue() {
        throw new UnsupportedOperationException("Not a ResultSingle.");
    }

    @Override
    public ValueList getList() {
        return value;
    }

    @Override
    public ValueList getColumn() {
        throw new UnsupportedOperationException("Not a ResultColumn.");
    }

    @Override
    public ResultList filterNulls() {
        return new ResultList(Result.filterNullsList(value));
    }

    @Override
    public ResultSingle first(int size) {
        return new ResultSingle(Result.firstList(value, size));
    }

    @Override
    public ResultSingle last(int size) {
        return new ResultSingle(Result.lastList(value, size));
    }

    @Override
    public ResultSingle random(int size) {
        return new ResultSingle(Result.randomList(value, size));
    }

    @Override
    public ResultList convertTo(Type to) {
        if (value.isNull()) {
            return new ResultList(new ValueList(null, to));
        }
        List<Value> result = value.getValue().stream().map(v -> v.convertTo(to)).collect(Collectors.toList());
        return new ResultList(new ValueList(result, to));
    }

    @Override
    public ResultSingle isNull() {
        return new ResultSingle(new ValueBoolean(value.isNull()));
    }

    @Override
    public Type getType() {
        return ((TypeCollection) value.getType()).getElementType();
    }
}
