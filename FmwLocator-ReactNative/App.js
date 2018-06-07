/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  Platform, StyleSheet, Text, TextInput, Button, View
} from 'react-native';

import Amplify, {
  Auth, Hub, Logger
} from 'aws-amplify';

import {
  Authenticator, withAuthenticator
} from 'aws-amplify-react-native';

import config from './aws-exports';

Amplify.configure(config);

const logger = new Logger('App');

class App extends Component {
  constructor(props) {
    super(props);
  Hub.listen('auth', this, 'onHubCapsule');
    this.state = {
      username: 'unknownUser',
      authenticated: false,
      tokens: {}
    };
  }

  // Default handler for listening events
  onHubCapsule(capsule) {
    const { channel, payload } = capsule;
    if (channel === 'auth') { 
      onAuthEvent(payload);
    }
  }
  onAuthEvent(payload) {
    const { event, data } = payload;
    switch (event) {
      case 'signIn':
          logger.debug('user signed in');
          break;
      case 'signUp':
        logger.debug('user signed up');
        break;
      case 'signOut':
          logger.debug('user signed out');
          break;
      case 'signIn_failure':
          logger.debug('user sign in failed');
          break;
    }
  }

  componentDidMount() {
    Auth.currentAuthenticatedUser()
      .then(user => this.setState(
        {
          username: user.username,
          authenticated: true
        }))
      .catch(err => this.setState(
        {
          authenticated: false
        }));
  }
  componentWillUnmount() {
    
  }
  
  render() {
    return (
      <View style={styles.container}>
        <Text>Welcome To this FMW</Text>
        <Text> {this.state.username} is Authenticated</Text>
       
      </View>
    );
  }
}
export default withAuthenticator(App);


const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});
