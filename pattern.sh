#!/bin/bash


r=$(( $RANDOM  ));

pattern=( beavis.zen blowfish bong bud-frogs bunny cheese cower daemon default dragon dragon-and-cow elephant elephant-in-snake eyes flaming-sheep ghostbusters head-in hellokitty kiss kitty koala kosh luke-koala meow milk moofasa moose mutilated ren satanic sheep skeleton small sodomized stegosaurus stimpy supermilker surgery telebears three-eyes turkey turtle tux udder vader vader-koala www )

#echo $r  ${#pattern[@]} $RANDOM % ${#pattern[@]}  $[$r % ${#pattern[@]}]
rand=$[$RANDOM % ${#pattern[@]}]

#echo $rand ${pattern[$rand]}
#echo ${pattern[49]}

fortune | cowsay -f ${pattern[$rand]} | lolcat
