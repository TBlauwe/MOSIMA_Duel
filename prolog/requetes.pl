use_module(library(jpl)).

explore_points(OffSize,DefSize):-
	OffSize>=2*DefSize. /* true if search for defensive */

being_attacked(Time):-
	Time<10.

areaCovered(Radius, Size, MapWidth):-
	2*pi*Radius*Size<0.6*MapWidth.

inGoodHealth(life):-
	life>3.

shotImpact(Probability):-
	Probability>0.1.

/* DECISIONS */
toOpenFire(EnemyInSight, P):-
	shotImpact(P),
	EnemyInSight.

explore(Time, Size, Radius, MapWidth, Class):-
	not(being_attacked(Time)),
	areaCovered(Radius, Size, MapWidth),
	jpl_call(Class, executeExplore, [], @(void)).

attack(EnemyInSight, Class):-
	EnemyInSight,
	jpl_call(Class, executeAttack, [], @(void)).