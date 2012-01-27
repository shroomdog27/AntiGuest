package de.codeinfection.quickwango.AntiGuest;

/**
 *
 * @author CodeInfection
 */
public class Vector
{
    private final double[] coords;
    private final short dimension;

    public Vector(double... coords)
    {
        this.coords = coords;
        this.dimension = (short)coords.length;
    }

    public Vector(int... coords)
    {
        this.dimension = (short)coords.length;
        this.coords = new double[this.dimension];
        for (short i = 0; i < this.dimension; ++i)
        {
            this.coords[i] = (double)coords[i];
        }
    }
    
    private void validate(Vector vec)
    {
        if (!vec.isCompatible(this))
        {
            throw new IllegalArgumentException("Given vector has another dimension!");
        }
    }
    
    public boolean isCompatible(Vector other)
    {
        return (this.dimension == other.getDimension());
    }
    
    public short getDimension()
    {
        return this.dimension;
    }
    
    public double get(int i)
    {
        return coords[i];
    }

    public boolean isOrthogonal(Vector other)
    {
        return (this.dotProduct(other) == 0);
    }

    public boolean isParallel(Vector other)
    {
        validate(other);
        double quotient = this.coords[0] / other.coords[0];
        for (int i = 1; i < this.dimension; ++i)
        {
            if (quotient != (this.coords[i] / other.coords[i]))
            {
                return false;
            }
        }
        return true;
    }

    public double dotProduct(Vector other)
    {
        validate(other);
        double sum = 0.0;
        for (short i = 0; i < this.dimension; ++i)
        {
            sum += this.get(i) * other.get(i);
        }
        return sum;
    }

    public Vector add(Vector other)
    {
        validate(other);
        double[] newCoords = new double[this.dimension];
        for (short i = 0; i < this.dimension; ++i)
        {
            newCoords[i] = this.get(i) + other.get(i);
        }
        return new Vector(newCoords);
    }

    public Vector substract(Vector other)
    {
        validate(other);
        double[] newCoords = new double[this.dimension];
        for (short i = 0; i < this.dimension; ++i)
        {
            newCoords[i] = this.get(i) - other.get(i);
        }
        return new Vector(newCoords);
    }

    public Vector multiply(int n)
    {
        return this.multiply((double)n);
    }
    
    public Vector multiply(double n)
    {
        double[] newCoords = new double[this.dimension];
        for (short i = 0; i < this.dimension; ++i)
        {
            newCoords[i] = this.get(i) * n;
        }
        return new Vector(newCoords);
    }

    public Vector divide(int n)
    {
        return this.divide((double)n);
    }

    public Vector divide(double n)
    {
        double[] newCoords = new double[this.dimension];
        for (short i = 0; i < this.dimension; ++i)
        {
            newCoords[i] = this.get(i) / n;
        }
        return new Vector(newCoords);
    }

    public double squaredLength()
    {
        double sum = 0;
        for (short i = 0; i < this.dimension; ++i)
        {
            sum += Math.pow(this.get(i), 2);
        }
        return sum;
    }

    public double length()
    {
        return Math.sqrt(this.squaredLength());
    }

    public Vector distanceVector(Vector other)
    {
        return other.substract(this);
    }

    public double distance(Vector other)
    {
        return this.distanceVector(other).length();
    }

    public double squaredDistance(Vector other)
    {
        return this.distanceVector(other).squaredLength();
    }

    public double crossAngle(Vector other)
    {
        return this.crossAngle(other, true);
    }

    public double crossAngle(Vector other, boolean degree)
    {
        double result = Math.acos(this.dotProduct(other) / (this.length() * other.length()));
        if (degree)
        {
            result *= 180 / Math.PI;
        }
        return result;
    }
    
    public Vector normalize()
    {
        return this.divide(this.length());
    }
    
    public Vector midpoint(Vector other)
    {
        return this.add(other.substract(this).divide(2));
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("(");
        sb.append(this.get(0));
        for (int i = 1; i < this.dimension; ++i)
        {
            sb.append("|").append(this.get(i));
        }
        return sb.append(")").toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        
        for (short i = 0; i < this.dimension; ++i)
        {
            hash = 79 * hash + (int)(Double.doubleToLongBits(this.coords[i]) ^ (Double.doubleToLongBits(this.coords[i]) >>> 32));
        }
        
        return hash;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other == null)
        {
            return false;
        }
        if (this.getClass() != other.getClass())
        {
            return false;
        }
        
        Vector otherVec = (Vector)other;
        if (!otherVec.isCompatible(this))
        {
            return false;
        }
        
        for (short i = 0; i < this.dimension; ++i)
        {
            if (this.get(i) != otherVec.get(i))
            {
                return false;
            }
        }
        
        return true;
    }
}