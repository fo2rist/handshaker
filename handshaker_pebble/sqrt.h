float my_sqrt( float num )
{
  float a, p, e = 0.001, b;
  
  a = num;
  p = a * a;
  while( p - num >= e )
  {
    b = ( a + ( num / a ) ) / 2;
    a = b;
    p = a * a;
  }
  return a;
}