package com.mikront.util;

import org.jetbrains.annotations.Contract;


@SuppressWarnings("unused UnusedReturnValue")
public class Concat {
    public static final String LINE_SEPARATOR = System.lineSeparator();

    private final StringBuilder builder = new StringBuilder();


    private Concat() {
    }

    @Contract("-> new")
    public static Concat me() {
        return new Concat();
    }


    public interface Mapper<E> {
        String map(E item);
    }


    public Concat word(Object object) {
        builder.append(object);
        return this;
    }

    public Concat words(Object... objects) {
        for (var o : objects)
            word(o);
        return this;
    }

    public <T extends Iterable<E>, E> Concat words(T from, Mapper<E> thisWay) {
        for (E e : from)
            word(thisWay.map(e));
        return this;
    }

    public Concat line() {
        if (!builder.isEmpty())
            builder.append(LINE_SEPARATOR);
        return this;
    }

    public Concat line(Object object) {
        line();
        return word(object);
    }

    public Concat lines(Object... objects) {
        for (var o : objects)
            line(o);
        return this;
    }

    public <T extends Iterable<E>, E> Concat lines(T from, Mapper<E> thisWay) {
        for (E e : from)
            line(thisWay.map(e));
        return this;
    }


    /**
     * Imitates 'if' clause
     * <br>
     * If the original expression is true, allows modifications. Otherwise, all modifications are
     * ignored until you call one of the {@link ConcatWhen} control methods {@link ConcatWhen#otherwise()},
     * {@link ConcatWhen#otherwise(boolean)}, or {@link ConcatWhen#then()}
     *
     * @param expression an expression to check
     * @return a new {@link ConcatWhen} instance
     */
    public ConcatWhen when(boolean expression) {
        return new ConcatWhen(this, expression);
    }

    public static class ConcatWhen extends Concat {
        private final Concat concat;

        private boolean expression;


        private ConcatWhen(Concat concat, boolean expression) {
            this.concat = concat;
            this.expression = expression;
        }


        @Override
        public ConcatWhen word(Object object) {
            if (expression)
                concat.word(object);
            return this;
        }

        @Override
        public ConcatWhen words(Object... objects) {
            if (expression)
                concat.words(objects);
            return this;
        }

        @Override
        public <T extends Iterable<E>, E> ConcatWhen words(T from, Mapper<E> thisWay) {
            if (expression)
                concat.words(from, thisWay);
            return this;
        }

        @Override
        public ConcatWhen line() {
            if (expression)
                concat.line();
            return this;
        }

        @Override
        public ConcatWhen line(Object object) {
            if (expression)
                concat.line(object);
            return this;
        }

        @Override
        public ConcatWhen lines(Object... objects) {
            if (expression)
                concat.lines(objects);
            return this;
        }

        @Override
        public <T extends Iterable<E>, E> ConcatWhen lines(T from, Mapper<E> thisWay) {
            if (expression)
                concat.lines(from, thisWay);
            return this;
        }


        /**
         * Don't call this method if you haven't used 'then' yet
         */
        @Override
        public ConcatWhen when(boolean expression) {
            return null;
        }

        /**
         * Negates the expression to imitate 'else' clause
         *
         * @return this object instance
         */
        public ConcatWhen otherwise() {
            expression = !expression;
            return this;
        }

        /**
         * Imitates 'else if' clause
         * <br>
         * If the original expression is true, does nothing. Otherwise, replaces the original to allow
         * the Concat instance modifications
         *
         * @param expression an expression to check
         * @return this object instance
         */
        public ConcatWhen otherwise(boolean expression) {
            //Invert the original expression
            otherwise();
            //The original expression is false, 'else' clause
            if (this.expression)
                this.expression = expression;
            return this;
        }

        public Concat then() {
            return concat;
        }


        @Override
        public String enate() {
            return concat.enate();
        }
    }


    public String enate() {
        return builder.toString();
    }

    @Override
    public String toString() {
        return enate();
    }
}